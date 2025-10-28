package com.studentgest.user_service.service;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.RolRepository;
import com.studentgest.user_service.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private RolRepository rolRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllUsers() {
        try {
            logger.debug("Iniciando recuperación de todos los usuarios");
            List<Object[]> results = entityManager.createQuery(
                            "SELECT u.id_usuario, u.nombre, u.apellido_paterno, u.apellido_materno, u.email, u.password, " +
                                    "u.id_rol, u.estado, u.foto, u.creado_en, u.activo FROM User u", Object[].class)
                    .getResultList();

            List<Map<String, Object>> users = new ArrayList<>();
            for (Object[] result : results) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id_usuario", result[0]);
                userMap.put("nombre", result[1]);
                userMap.put("apellido_paterno", result[2]);
                userMap.put("apellido_materno", result[3]);
                userMap.put("email", result[4]);
                userMap.put("password", result[5]);
                userMap.put("id_rol", result[6]); // Puede ser null
                userMap.put("estado", result[7] != null ? result[7].toString() : null);
                userMap.put("foto", result[8]);
                userMap.put("creado_en", result[9]);
                userMap.put("activo", result[10]);
                users.add(userMap);
            }
            logger.debug("Recuperados {} usuarios de la base de datos", users.size());
            users.forEach(user -> logger.debug("Usuario: id={}, email={}, id_rol={}, estado={}",
                    user.get("id_usuario"), user.get("email"), user.get("id_rol"), user.get("estado")));
            return users;
        } catch (Exception e) {
            logger.error("Error al obtener todos los usuarios", e);
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Integer id) {
        logger.debug("Buscando usuario con ID: {}", id);
        try {
            Optional<User> user = repository.findById(id);
            logger.debug("Resultado de findById: {}", user.isPresent() ? "Encontrado" : "No encontrado");
            return user;
        } catch (Exception e) {
            logger.error("Error al buscar usuario con ID: {}", id, e);
            throw new RuntimeException("Error al buscar usuario con ID: " + id, e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        try {
            return Optional.ofNullable(repository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Email no encontrado: " + email)));
        } catch (Exception e) {
            logger.error("Error al buscar usuario por email: {}", email, e);
            throw new RuntimeException("Error al buscar usuario por email", e);
        }
    }

    @Transactional
    public User createUser(User user) {
        try {
            if (user.getId_rol() == null && user.getRol() != null && user.getRol().getIdRol() == null) {
                String rolNombre = user.getRol().getNombre();
                Rol rol = rolRepository.findByNombre(rolNombre)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));
                user.setId_rol(rol.getIdRol());
            } else if (user.getId_rol() != null) {
                rolRepository.findById(user.getId_rol())
                        .orElseThrow(() -> new RuntimeException("Rol con ID " + user.getId_rol() + " no encontrado"));
            }
            if (user.getEmail() == null || user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Email y contraseña son requeridos");
            }
            Optional<User> existingUser = repository.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                throw new IllegalArgumentException("El email " + user.getEmail() + " ya está en uso");
            }
            // Hashear la contraseña antes de guardar
            user.setPassword(hashPassword(user.getPassword()));
            // Establecer estado PENDIENTE y activo = true por defecto
            user.setEstado(EstadoUsuario.PENDIENTE);
            user.setActivo(true);
            user.setCreado_en(LocalDateTime.now());
            return repository.save(user);
        } catch (Exception e) {
            logger.error("Error al crear usuario", e);
            throw new RuntimeException("Error al crear usuario", e);
        }
    }

    @Transactional
    public User updateUser(Integer id, User userDetails) {
        try {
            return repository.findById(id)
                    .map(user -> {
                        if (userDetails.getNombre() != null) user.setNombre(userDetails.getNombre());
                        if (userDetails.getApellido_paterno() != null) user.setApellido_paterno(userDetails.getApellido_paterno());
                        if (userDetails.getApellido_materno() != null) user.setApellido_materno(userDetails.getApellido_materno());
                        if (userDetails.getEmail() != null) {
                            Optional<User> existingUser = repository.findByEmail(userDetails.getEmail());
                            if (existingUser.isPresent() && !existingUser.get().getId_usuario().equals(id)) {
                                throw new IllegalArgumentException("El email " + userDetails.getEmail() + " ya está en uso por otro usuario");
                            }
                            user.setEmail(userDetails.getEmail());
                        }
                        if (userDetails.getId_rol() != null) {
                            rolRepository.findById(userDetails.getId_rol())
                                    .orElseThrow(() -> new RuntimeException("Rol con ID " + userDetails.getId_rol() + " no encontrado"));
                            user.setId_rol(userDetails.getId_rol());
                        }
                        if (userDetails.getFoto() != null) user.setFoto(userDetails.getFoto());
                        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                            user.setPassword(hashPassword(userDetails.getPassword()));
                        }
                        if (userDetails.getEstado() != null) user.setEstado(userDetails.getEstado());
                        if (userDetails.isActivo() != user.isActivo()) user.setActivo(userDetails.isActivo());
                        return repository.save(user);
                    })
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        } catch (Exception e) {
            logger.error("Error al actualizar usuario con ID: {}", id, e);
            throw new RuntimeException("Error al actualizar usuario con ID: " + id, e);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(String email, String password) {
        try {
            logger.debug("Iniciando login para email: {}", email);
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                logger.warn("Email o contraseña vacíos: email='{}', password='{}'", email, password);
                return Map.of("success", false, "message", "Email y contraseña son requeridos");
            }
            Optional<User> userOptional = repository.findByEmail(email);
            if (!userOptional.isPresent()) {
                logger.warn("Usuario no encontrado para email: {}", email);
                return Map.of("success", false, "message", "Credenciales incorrectas");
            }
            User user = userOptional.get();
            logger.debug("Usuario encontrado: id={}, estado={}, activo={}", user.getId_usuario(), user.getEstado(), user.isActivo());
            if (!EstadoUsuario.APROBADO.equals(user.getEstado())) {
                logger.warn("Usuario no aprobado: id={}, estado={}", user.getId_usuario(), user.getEstado());
                return Map.of("success", false, "message", "Usuario no aprobado");
            }
            if (!user.isActivo()) {
                logger.warn("Usuario inactivo: id={}", user.getId_usuario());
                return Map.of("success", false, "message", "Usuario inactivo");
            }
            if (verifyPassword(password, user.getPassword())) {
                logger.info("Login exitoso para usuario: id={}", user.getId_usuario());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("id", user.getId_usuario());
                response.put("nombre", user.getNombre());
                response.put("apellido_paterno", user.getApellido_paterno());
                response.put("apellido_materno", user.getApellido_materno());
                response.put("email", user.getEmail());
                response.put("rol", user.getRol() != null ? user.getRol().getNombre() : null);
                response.put("id_rol", user.getId_rol()); // Agregado para incluir id_rol
                response.put("foto", user.getFoto());
                return response;
            } else {
                logger.warn("Contraseña incorrecta para usuario: id={}", user.getId_usuario());
                return Map.of("success", false, "message", "Contraseña incorrecta");
            }
        } catch (Exception e) {
            logger.error("Error al procesar login para email: {}", email, e);
            return Map.of("success", false, "message", "Error al iniciar sesión: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Map<String, String> getRolUsuario(Integer id) {
        try {
            return getUserById(id)
                    .map(user -> Map.of("success", "true", "rol", user.getRol() != null ? user.getRol().getNombre() : null))
                    .orElse(Map.of("success", "false", "message", "Usuario no encontrado"));
        } catch (Exception e) {
            logger.error("Error al obtener rol para usuario con ID: {}", id, e);
            return Map.of("success", "false", "message", "Error al obtener rol");
        }
    }

    @Transactional
    public Map<String, String> resetPassword(String email) {
        try {
            User user = getUserByEmail(email)
                    .filter(User::isActivo)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado o inactivo"));
            String newPassword = "ABCabc1234!";
            user.setPassword(hashPassword(newPassword));
            repository.save(user);
            return Map.of("success", "true", "message", "Contraseña reseteada con éxito, notificación pendiente.");
        } catch (Exception e) {
            logger.error("Error al resetear la contraseña para el usuario con email: {}", email, e);
            return Map.of("success", "false", "message", "Error al resetear la contraseña");
        }
    }

    @Transactional
    public void desactivarUsuario(Integer id) {
        try {
            User user = getUserById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));
            user.setActivo(false);
            repository.save(user);
            logger.info("Usuario desactivado correctamente con ID: {}", id);
        } catch (Exception e) {
            logger.error("Error al desactivar usuario con ID: {}", id, e);
            throw new RuntimeException("Error al desactivar el usuario", e);
        }
    }

    @Transactional(readOnly = true)
    public List<User> getUsuariosActivos() {
        try {
            logger.debug("Iniciando recuperación de usuarios activos");
            List<User> users = repository.findByActivoTrue();
            logger.debug("Recuperados {} usuarios activos", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error al obtener usuarios activos", e);
            throw new RuntimeException("Error al recuperar usuarios activos", e);
        }
    }

    @Transactional
    public void activarUsuario(Integer id) {
        try {
            User user = getUserById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));
            if (user.isActivo()) {
                logger.warn("El usuario con ID: {} ya está activo", id);
                throw new IllegalStateException("El usuario ya está activo");
            }
            user.setActivo(true);
            if (user.getEstado() != EstadoUsuario.APROBADO) {
                user.setEstado(EstadoUsuario.APROBADO);
                logger.debug("Estado del usuario con ID: {} cambiado a APROBADO", id);
            }
            if (user.getCreado_en() == null) {
                user.setCreado_en(LocalDateTime.now());
                logger.debug("Fecha de creación establecida para usuario con ID: {}", id);
            }
            repository.save(user);
            logger.info("Usuario activado correctamente con ID: {}", id);
        } catch (NoSuchElementException e) {
            logger.error("Usuario no encontrado para activar con ID: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error al activar usuario con ID: {}", id, e);
            throw new RuntimeException("Error al activar el usuario: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<User> getUsuariosPorRol(String rolNombre) {
        try {
            logger.debug("Iniciando recuperación de usuarios por rol: {}", rolNombre);
            Rol rol = rolRepository.findByNombre(rolNombre)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));
            Integer idRol = rol.getIdRol();
            logger.debug("ID del rol encontrado: {}", idRol);
            List<User> users = repository.findByIdRol(idRol);
            logger.debug("Recuperados {} usuarios para rol: {}", users.size(), rolNombre);
            return users;
        } catch (Exception e) {
            logger.error("Error al obtener usuarios por rol: {}", rolNombre, e);
            throw new RuntimeException("Error al recuperar usuarios por rol", e);
        }
    }

    @Transactional
    public void aprobarUsuario(Integer id) {
        try {
            User user = getUserById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));
            if (user.getEstado() == EstadoUsuario.APROBADO) {
                logger.warn("El usuario con ID: {} ya está aprobado", id);
                throw new IllegalStateException("El usuario ya está aprobado");
            }
            user.setEstado(EstadoUsuario.APROBADO);
            if (user.getCreado_en() == null) {
                user.setCreado_en(LocalDateTime.now());
                logger.debug("Fecha de creación establecida para usuario con ID: {}", id);
            }
            repository.save(user);
            logger.info("Usuario aprobado correctamente con ID: {}", id);
        } catch (NoSuchElementException e) {
            logger.error("Usuario no encontrado para aprobar con ID: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error al aprobar usuario con ID: {}", id, e);
            throw new RuntimeException("Error al aprobar el usuario: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void desaprobarUsuario(Integer id) {
        try {
            User user = getUserById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));
            if (user.getEstado() == EstadoUsuario.RECHAZADO) {
                logger.warn("El usuario con ID: {} ya está rechazado", id);
                throw new IllegalStateException("El usuario ya está rechazado");
            }
            user.setEstado(EstadoUsuario.RECHAZADO);
            repository.save(user);
            logger.info("Usuario desaprobado correctamente con ID: {}", id);
        } catch (NoSuchElementException e) {
            logger.error("Usuario no encontrado para desaprobar con ID: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error al desaprobar usuario con ID: {}", id, e);
            throw new RuntimeException("Error al desaprobar el usuario: " + e.getMessage(), e);
        }
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        try {
            if (rawPassword == null || hashedPassword == null) {
                logger.warn("Contraseña nula: rawPassword={}, hashedPassword={}", rawPassword, hashedPassword);
                return false;
            }
            boolean matches = hashPassword(rawPassword).equals(hashedPassword);
            logger.debug("Verificación de contraseña: coincide={}", matches);
            return matches;
        } catch (Exception e) {
            logger.error("Error al verificar contraseña", e);
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            logger.debug("Contraseña hasheada: longitud={}", sb.length());
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error al hashear la contraseña", e);
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

    @Transactional
    public void hashExistingPasswords() {
        try {
            List<User> users = repository.findAll();
            logger.info("Iniciando hasheo de contraseñas existentes: {} usuarios encontrados", users.size());
            for (User user : users) {
                String currentPassword = user.getPassword();
                if (currentPassword != null && !currentPassword.isEmpty() && currentPassword.length() < 64) {
                    user.setPassword(hashPassword(currentPassword));
                    repository.save(user);
                    logger.info("Contraseña hasheada para usuario con ID: {}", user.getId_usuario());
                }
            }
            logger.info("Hasheo de contraseñas existentes completado");
        } catch (Exception e) {
            logger.error("Error al hashear contraseñas existentes", e);
            throw new RuntimeException("Error al hashear contraseñas existentes", e);
        }
    }
}