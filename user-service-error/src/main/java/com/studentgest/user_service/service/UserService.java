package com.studentgest.user_service.service;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.UserRepository;
import com.studentgest.user_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordHistoryService passwordHistoryService;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityConfigService securityConfigService;

    // ========================= MÉTODOS BÁSICOS =========================

    public List<User> obtenerTodos() {
        return repository.findAll();
    }

    public List<User> obtenerUsuariosActivos() {
        return repository.findByActivoTrue();
    }

    public Optional<User> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Optional<User> obtenerPorEmail(String email) {
        return repository.findByEmail(email.toLowerCase().trim());
    }

    public Optional<User> obtenerPorDocumentoIdentidad(String documento) {
        return repository.findByDocumentoIdentidad(documento);
    }

    public Optional<User> obtenerPorUserId(String userId) {
        return repository.findByUserId(userId);
    }

    public List<User> obtenerPorRol(Long idRol) {
        return repository.findByIdRol(idRol);
    }

    // ========================= CREAR USUARIO =========================
    public User crearUsuario(User user) {
        try {
            logger.info("=== CREANDO USUARIO ===");
            logger.info("Email: {}", user.getEmail());
            logger.info("Nombre: {}", user.getNombre());
            logger.info("Rol ID: {}", user.getIdRol());

            // Validaciones básicas
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("El email es requerido");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es requerida");
            }
            if (user.getIdRol() == null) {
                throw new IllegalArgumentException("El idRol es requerido");
            }
            if (user.getDocumentoIdentidad() == null || user.getDocumentoIdentidad().length() < 10) {
                throw new IllegalArgumentException("El documento de identidad debe tener al menos 10 caracteres.");
            }

            String email = user.getEmail().toLowerCase().trim();

            // Validar duplicados
            if (repository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("El email ya está registrado");
            }
            if (repository.existsByDocumentoIdentidad(user.getDocumentoIdentidad())) {
                throw new IllegalArgumentException("El documento de identidad ya está registrado");
            }

            // Validar política de contraseñas
            if (!passwordPolicyService.validatePassword(user.getPassword())) {
                throw new IllegalArgumentException("La contraseña no cumple con las políticas de seguridad: "
                        + passwordPolicyService.getPasswordRequirements());
            }

            // Hash de contraseña
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            logger.info("🔐 Contraseña hasheada correctamente");

            // Crear nuevo usuario
            User newUser = new User();
            newUser.setNombre(user.getNombre());
            newUser.setApellido_paterno(user.getApellido_paterno());
            newUser.setApellido_materno(user.getApellido_materno());
            newUser.setEmail(email);
            newUser.setPassword(hashedPassword);
            newUser.setIdRol(user.getIdRol());
            newUser.setDocumentoIdentidad(user.getDocumentoIdentidad());
            newUser.setEstado(EstadoUsuario.APROBADO);
            newUser.setActivo(true);
            newUser.setIntentosFallidos(0);
            newUser.setBloqueado(false);
            newUser.setRequiresPasswordChange(false);

            // Generar userId = documento + primera letra del apellido + primera letra del nombre
            String apellido = (user.getApellido_paterno() != null && !user.getApellido_paterno().isEmpty())
                    ? user.getApellido_paterno().substring(0, 1).toUpperCase() : "";
            String nombre = (user.getNombre() != null && !user.getNombre().isEmpty())
                    ? user.getNombre().substring(0, 1).toUpperCase() : "";
            String userId = user.getDocumentoIdentidad() + apellido + nombre;
            newUser.setUserId(userId);

            // Timestamps
            newUser.setCreado_en(new Timestamp(System.currentTimeMillis()));
            newUser.setUltimoCambioPassword(new Timestamp(System.currentTimeMillis()));

            // Expiración de contraseña (90 días)
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(90);
            newUser.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));

            User savedUser = repository.save(newUser);

            // Guardar historial
            passwordHistoryService.addToPasswordHistory(savedUser.getId_usuario(), hashedPassword);

            logger.info("✅ Usuario creado exitosamente: {}", savedUser.getEmail());
            return savedUser;

        } catch (DataIntegrityViolationException e) {
            logger.error("❌ Error de integridad de datos: {}", e.getMessage());
            throw new IllegalArgumentException("Error de base de datos: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error de validación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("💥 ERROR inesperado al crear usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno al crear usuario: " + e.getMessage(), e);
        }
    }

    // ========================= LOGIN =========================
    public Map<String, Object> login(String email, String password) {
        logger.info("=== INICIANDO LOGIN ===");
        Optional<User> userOptional = obtenerPorEmail(email);

        if (userOptional.isEmpty()) {
            auditLogService.logLoginAttempt(email, false, "unknown");
            return Map.of("success", false, "message", "Credenciales incorrectas");
        }

        User user = userOptional.get();

        if (user.isBloqueado()) {
            auditLogService.logLoginAttempt(email, false, "unknown");
            return Map.of("success", false, "message", "Cuenta bloqueada. Contacte al administrador.");
        }

        if (!EstadoUsuario.APROBADO.equals(user.getEstado()) || !user.isActivo()) {
            auditLogService.logLoginAttempt(email, false, "unknown");
            return Map.of("success", false, "message", "Cuenta no activa o pendiente de aprobación");
        }

        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
        if (passwordMatch) {
            user.setIntentosFallidos(0);
            repository.save(user);

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getIdRol() != null ? user.getIdRol().toString() : "0",
                    user.getId_usuario()
            );

            auditLogService.logLoginAttempt(email, true, "unknown");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("id", user.getId_usuario());
            response.put("nombre", user.getNombre());
            response.put("email", user.getEmail());
            response.put("idRol", user.getIdRol());
            response.put("userId", user.getUserId());
            response.put("sessionTimeout", user.getSessionTimeout());
            response.put("requiresPasswordChange", user.isRequiresPasswordChange());
            return response;
        } else {
            user.setIntentosFallidos(user.getIntentosFallidos() + 1);
            if (user.getIntentosFallidos() >= 3) {
                user.setBloqueado(true);
                auditLogService.logAccountLocked(email, "unknown");
            }
            repository.save(user);
            auditLogService.logLoginAttempt(email, false, "unknown");
            return Map.of("success", false, "message",
                    user.isBloqueado() ? "Cuenta bloqueada por múltiples intentos" : "Credenciales incorrectas");
        }
    }

    // ========================= UTILIDADES =========================

    public User actualizarUsuario(Long id, User datosActualizados) {
        return repository.findById(id).map(usuario -> {
            usuario.setNombre(datosActualizados.getNombre());
            usuario.setApellido_paterno(datosActualizados.getApellido_paterno());
            usuario.setApellido_materno(datosActualizados.getApellido_materno());
            usuario.setEmail(datosActualizados.getEmail());
            usuario.setIdRol(datosActualizados.getIdRol());
            usuario.setDocumentoIdentidad(datosActualizados.getDocumentoIdentidad());
            usuario.setActivo(datosActualizados.isActivo());
            usuario.setEstado(datosActualizados.getEstado());
            return repository.save(usuario);
        }).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));
    }

    public void eliminarUsuario(Long id) {
        repository.deleteById(id);
    }

    public User activarUsuario(Long id) {
        return repository.findById(id).map(user -> {
            user.setActivo(true);
            user.setEstado(EstadoUsuario.APROBADO);
            return repository.save(user);
        }).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado para activar"));
    }

    public User desactivarUsuario(Long id) {
        return repository.findById(id).map(user -> {
            user.setActivo(false);
            return repository.save(user);
        }).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado para desactivar"));
    }

    public void resetPassword(String identificador, String nuevaPassword) {
        Optional<User> userOpt = repository.findByEmail(identificador);
        if (userOpt.isEmpty()) {
            userOpt = repository.findByDocumentoIdentidad(identificador);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String hashedPassword = passwordEncoder.encode(nuevaPassword);
            user.setPassword(hashedPassword);
            user.setRequiresPasswordChange(false);
            user.setUltimoCambioPassword(new Timestamp(System.currentTimeMillis()));
            repository.save(user);
            passwordHistoryService.addToPasswordHistory(user.getId_usuario(), hashedPassword);
        } else {
            throw new NoSuchElementException("Usuario no encontrado con identificador: " + identificador);
        }
    }

    public void desbloquearUsuario(Long id) {
        repository.findById(id).ifPresent(user -> {
            user.setBloqueado(false);
            user.setIntentosFallidos(0);
            repository.save(user);
        });
    }
}
