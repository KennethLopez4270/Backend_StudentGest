package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.service.UserService;
import com.studentgest.user_service.repository.RolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RolRepository rolRepository; // Inyectar RolRepository

    private final RestTemplate restTemplate = new RestTemplate();
    private final String notificationUrl = "http://localhost:8080/api/notifications";

    @GetMapping
    public List<Map<String, Object>> getAllUsers() {
        try {
            List<Map<String, Object>> users = userService.getAllUsers();
            return users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>(user);
                Integer idRol = (Integer) user.get("id_rol"); // Puede ser null
                if (idRol != null) {
                    try {
                        String rolNombre = rolRepository.findById(idRol) // Usar la instancia inyectada
                                .map(Rol::getNombre)
                                .orElse("Sin rol");
                        userMap.put("rol", rolNombre);
                    } catch (Exception e) {
                        userMap.put("rol", "Sin rol");
                        logger.warn("No se pudo obtener el nombre del rol para id_rol: {}", idRol, e);
                    }
                } else {
                    userMap.put("rol", "Sin rol"); // Manejo explícito cuando id_rol es null
                }
                return userMap;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener todos los usuarios", e);
            return List.of();
        }
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Integer id) {
        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                User u = user.get();
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id_usuario", u.getId_usuario());
                userMap.put("nombre", u.getNombre());
                userMap.put("apellido_paterno", u.getApellido_paterno());
                userMap.put("apellido_materno", u.getApellido_materno());
                userMap.put("email", u.getEmail());
                userMap.put("password", u.getPassword());
                userMap.put("id_rol", u.getId_rol());
                userMap.put("rol", u.getRol() != null ? u.getRol().getNombre() : null);
                userMap.put("estado", u.getEstado());
                userMap.put("foto", u.getFoto());
                userMap.put("creado_en", u.getCreado_en());
                userMap.put("activo", u.isActivo());
                return userMap;
            }
            return Map.of();
        } catch (Exception e) {
            logger.error("Error al obtener usuario con ID: {}", id, e);
            return Map.of();
        }
    }

    @GetMapping("/activos")
    public List<User> getUsuariosActivos() {
        try {
            return userService.getUsuariosActivos();
        } catch (Exception e) {
            logger.error("Error al obtener usuarios activos", e);
            return List.of();
        }
    }

    @GetMapping("/rol/{rol}")
    public List<User> getUsuariosPorRol(@PathVariable String rol) {
        try {
            return userService.getUsuariosPorRol(rol);
        } catch (Exception e) {
            logger.error("Error al obtener usuarios por rol: {}", rol, e);
            return List.of();
        }
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        try {
            return userService.createUser(user);
        } catch (Exception e) {
            logger.error("Error al crear usuario", e);
            throw new RuntimeException("Error al crear usuario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> userDetails) {
        try {
            Optional<User> userOptional = userService.getUserById(id);
            if (!userOptional.isPresent()) {
                throw new RuntimeException("Usuario no encontrado con ID: " + id);
            }
            User user = userOptional.get();
            if (userDetails.containsKey("nombre")) {
                user.setNombre((String) userDetails.get("nombre"));
            }
            if (userDetails.containsKey("apellido_paterno")) {
                user.setApellido_paterno((String) userDetails.get("apellido_paterno"));
            }
            if (userDetails.containsKey("apellido_materno")) {
                user.setApellido_materno((String) userDetails.get("apellido_materno"));
            }
            if (userDetails.containsKey("email")) {
                user.setEmail((String) userDetails.get("email"));
            }
            if (userDetails.containsKey("password")) {
                user.setPassword((String) userDetails.get("password"));
            }
            if (userDetails.containsKey("id_rol")) {
                user.setId_rol((Integer) userDetails.get("id_rol"));
            }
            if (userDetails.containsKey("estado")) {
                user.setEstado(userDetails.get("estado") != null ? EstadoUsuario.valueOf((String) userDetails.get("estado")) : null);
            }
            if (userDetails.containsKey("foto")) {
                user.setFoto((String) userDetails.get("foto"));
            }
            if (userDetails.containsKey("activo")) {
                user.setActivo((Boolean) userDetails.get("activo"));
            }
            User updatedUser = userService.updateUser(id, user);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id_usuario", updatedUser.getId_usuario());
            userMap.put("nombre", updatedUser.getNombre());
            userMap.put("apellido_paterno", updatedUser.getApellido_paterno());
            userMap.put("apellido_materno", updatedUser.getApellido_materno());
            userMap.put("email", updatedUser.getEmail());
            userMap.put("password", updatedUser.getPassword());
            userMap.put("id_rol", updatedUser.getId_rol());
            userMap.put("rol", updatedUser.getRol() != null ? updatedUser.getRol().getNombre() : null);
            userMap.put("estado", updatedUser.getEstado());
            userMap.put("foto", updatedUser.getFoto());
            userMap.put("creado_en", updatedUser.getCreado_en());
            userMap.put("activo", updatedUser.isActivo());
            return userMap;
        } catch (RuntimeException e) {
            logger.error("Error al actualizar usuario con ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                logger.warn("Credenciales inválidas: email='{}', password='{}'", email, password);
                return Map.of("message", "Email y contraseña son requeridos");
            }
            Map<String, Object> response = userService.login(email, password);
            if (Boolean.TRUE.equals(response.get("success"))) {
                Map<String, Object> adjustedResponse = new HashMap<>();
                adjustedResponse.put("id", response.get("id"));
                adjustedResponse.put("nombre", response.get("nombre"));
                adjustedResponse.put("apellido_paterno", response.get("apellido_paterno"));
                adjustedResponse.put("apellido_materno", response.get("apellido_materno"));
                adjustedResponse.put("email", response.get("email"));
                adjustedResponse.put("rol", response.get("rol"));
                adjustedResponse.put("id_rol", response.get("id_rol")); // Agregado para incluir id_rol
                adjustedResponse.put("foto", response.get("foto"));
                return adjustedResponse;
            } else {
                return Map.of("message", response.get("message"));
            }
        } catch (Exception e) {
            logger.error("Error al procesar login para email: {}", credentials.get("email"), e);
            return Map.of("message", "Error al iniciar sesión: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public Map<String, String> getRolUsuario(@RequestParam Integer id) {
        try {
            Map<String, String> response = userService.getRolUsuario(id);
            if ("true".equals(response.get("success"))) {
                return Map.of("rol", response.get("rol"));
            }
            return Map.of("message", response.get("message"));
        } catch (Exception e) {
            logger.error("Error al obtener rol para usuario con ID: {}", id, e);
            return Map.of("message", "Error al obtener rol");
        }
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            Map<String, String> response = userService.resetPassword(email);
            if ("true".equals(response.get("success"))) {
                Optional<User> userOptional = userService.getUserByEmail(email);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    Map<String, String> notificationBody = new HashMap<>();
                    notificationBody.put("to", email);
                    notificationBody.put("nombre", user.getNombre());
                    notificationBody.put("nuevaPassword", "ABCabc1234!");
                    try {
                        restTemplate.postForObject(notificationUrl + "/reset-password", notificationBody, String.class);
                    } catch (Exception e) {
                        logger.warn("Error al enviar notificación de reset-password para email: {}", email, e);
                    }
                }
                return Map.of("message", "Se ha enviado un correo con la nueva contraseña.");
            }
            return Map.of("message", response.get("message"));
        } catch (Exception e) {
            logger.error("Error al resetear contraseña", e);
            return Map.of("message", "Error al resetear la contraseña");
        }
    }

    @PutMapping("/desactivar/{id}")
    public Map<String, String> desactivarUsuario(@PathVariable Integer id) {
        try {
            userService.desactivarUsuario(id);
            return Map.of("message", "Usuario desactivado correctamente.");
        } catch (NoSuchElementException e) {
            logger.error("Usuario no encontrado para desactivar", e);
            return Map.of("message", "Usuario no encontrado con ese ID.");
        } catch (Exception e) {
            logger.error("Error al desactivar usuario", e);
            return Map.of("message", "Error al desactivar el usuario.");
        }
    }

    @PutMapping("/activar/{id}")
    public Map<String, String> activarUsuario(@PathVariable Integer id) {
        try {
            userService.activarUsuario(id);
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.isActivo() && user.getEstado() == EstadoUsuario.APROBADO) {
                    Map<String, String> notificationBody = new HashMap<>();
                    notificationBody.put("to", user.getEmail());
                    notificationBody.put("nombre", user.getNombre());
                    try {
                        restTemplate.postForObject(notificationUrl + "/activate-account", notificationBody, String.class);
                        logger.info("Notificación de activación enviada para usuario con ID: {}", id);
                    } catch (Exception e) {
                        logger.warn("Error al enviar notificación de activación para usuario con ID: {}. Continuando sin notificación.", id, e);
                    }
                }
            }
            return Map.of("message", "Usuario activado correctamente.");
        } catch (NoSuchElementException e) {
            logger.error("Usuario no encontrado para activar con ID: {}", id, e);
            return Map.of("message", "Usuario no encontrado con ese ID.");
        } catch (IllegalStateException e) {
            logger.warn("No se puede activar usuario con ID: {} - {}", id, e.getMessage());
            return Map.of("message", e.getMessage());
        } catch (Exception e) {
            logger.error("Error al activar usuario con ID: {}", id, e);
            return Map.of("message", "Error al activar el usuario.");
        }
    }

    @PutMapping("/aprobar/{id}")
    public Map<String, String> aprobarUsuario(@PathVariable Integer id) {
        try {
            userService.aprobarUsuario(id);
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.getEstado() == EstadoUsuario.APROBADO) {
                    Map<String, String> notificationBody = new HashMap<>();
                    notificationBody.put("to", user.getEmail());
                    notificationBody.put("nombre", user.getNombre());
                    try {
                        restTemplate.postForObject(notificationUrl + "/approve-account", notificationBody, String.class);
                        logger.info("Notificación de aprobación enviada para usuario con ID: {}", id);
                    } catch (Exception e) {
                        logger.warn("Error al enviar notificación de aprobación para usuario con ID: {}. Continuando sin notificación.", id, e);
                    }
                }
            }
            return Map.of("message", "Usuario aprobado correctamente.");
        } catch (NoSuchElementException e) {
            logger.error("Usuario no encontrado para aprobar con ID: {}", id, e);
            return Map.of("message", "Usuario no encontrado con ese ID.");
        } catch (IllegalStateException e) {
            logger.warn("No se puede aprobar usuario con ID: {} - {}", id, e.getMessage());
            return Map.of("message", e.getMessage());
        } catch (Exception e) {
            logger.error("Error al aprobar usuario con ID: {}", id, e);
            return Map.of("message", "Error al aprobar el usuario.");
        }
    }

    @PutMapping("/desaprobar/{id}")
    public Map<String, String> desaprobarUsuario(@PathVariable Integer id) {
        try {
            userService.desaprobarUsuario(id);
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.getEstado() == EstadoUsuario.RECHAZADO) {
                    Map<String, String> notificationBody = new HashMap<>();
                    notificationBody.put("to", user.getEmail());
                    notificationBody.put("nombre", user.getNombre());
                    try {
                        restTemplate.postForObject(notificationUrl + "/reject-account", notificationBody, String.class);
                        logger.info("Notificación de desaprobación enviada para usuario con ID: {}", id);
                    } catch (Exception e) {
                        logger.warn("Error al enviar notificación de desaprobación para usuario con ID: {}. Continuando sin notificación.", id, e);
                    }
                }
            }
            return Map.of("message", "Usuario desaprobado correctamente.");
        } catch (NoSuchElementException e) {
            logger.error("Usuario no encontrado para desaprobar con ID: {}", id, e);
            return Map.of("message", "Usuario no encontrado con ese ID.");
        } catch (IllegalStateException e) {
            logger.warn("No se puede desaprobar usuario con ID: {} - {}", id, e.getMessage());
            return Map.of("message", e.getMessage());
        } catch (Exception e) {
            logger.error("Error al desaprobar usuario con ID: {}", id, e);
            return Map.of("message", "Error al desaprobar el usuario.");
        }
    }
}