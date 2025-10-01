package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.service.PasswordPolicyService;
import com.studentgest.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private NotificacionClient notificacionClient;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String sanitizeInput(String input) {
        if (input == null) return null;
        return HtmlUtils.htmlEscape(input.trim());
    }

    private String sanitizeEmail(String email) {
        if (email == null) return null;
        return HtmlUtils.htmlEscape(email.toLowerCase().trim());
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error al obtener usuarios", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error interno del servidor",
                "message", "No se pudieron obtener los usuarios"
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Usuario no encontrado",
                    "message", "El usuario con ID " + id + " no existe"
                ));
            }
        } catch (Exception e) {
            logger.error("Error al obtener usuario por ID", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error interno del servidor",
                "message", "No se pudo obtener el usuario"
            ));
        }
    }

    @PostMapping
public ResponseEntity<?> createUser(@RequestBody @Valid User user, HttpServletRequest request) {
    try {
        logger.info("=== INICIANDO REGISTRO ===");
        logger.info("Email: {}", user.getEmail());
        logger.info("Nombre: {}", user.getNombre());
        logger.info("Apellido Paterno: {}", user.getApellido_paterno());
        logger.info("Apellido Materno: {}", user.getApellido_materno());
        logger.info("Rol: {}", user.getRol());
        logger.info("Password length: {}", user.getPassword() != null ? user.getPassword().length() : "null");

        // Sanitizar y normalizar inputs
        user.setNombre(sanitizeInput(user.getNombre()));
        user.setApellido_paterno(sanitizeInput(user.getApellido_paterno()));
        user.setApellido_materno(sanitizeInput(user.getApellido_materno()));
        user.setEmail(sanitizeEmail(user.getEmail()));

        logger.info("✅ Datos sanitizados, llamando a userService...");
        
        User savedUser = userService.createUser(user);
        
        logger.info("✅ Usuario creado exitosamente: {}", savedUser.getEmail());
        logger.info("✅ ID generado: {}", savedUser.getId_usuario());
        logger.info("✅ Estado: {}", savedUser.getEstado());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Usuario registrado exitosamente",
            "user", Map.of(
                "id", savedUser.getId_usuario(),
                "nombre", savedUser.getNombre(),
                "email", savedUser.getEmail(),
                "rol", savedUser.getRol(),
                "estado", savedUser.getEstado()
            )
        ));

    } catch (IllegalArgumentException e) {
        logger.error("❌ Error de validación: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "error", "Error de validación",
            "message", e.getMessage()
        ));
    } catch (DataIntegrityViolationException e) {
        logger.error("❌ Error de base de datos: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "error", "Error de base de datos",
            "message", "El email ya está en uso"
        ));
    } catch (Exception e) {
        logger.error("💥 ERROR INTERNO: {}", e.getMessage(), e);
        // Log más detallado
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of(
            "success", false,
            "error", "Error interno del servidor",
            "message", e.getMessage() != null ? e.getMessage() : "Error desconocido",
            "details", e.getClass().getName()
        ));
    }
}

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody @Valid User user) {
        try {
            // Sanitizar inputs
            user.setNombre(sanitizeInput(user.getNombre()));
            user.setApellido_paterno(sanitizeInput(user.getApellido_paterno()));
            user.setApellido_materno(sanitizeInput(user.getApellido_materno()));
            user.setEmail(sanitizeEmail(user.getEmail()));

            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario actualizado exitosamente",
                "user", updatedUser
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "error", "Usuario no encontrado"
            ));
        } catch (Exception e) {
            logger.error("Error al actualizar usuario", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        try {
            String email = sanitizeEmail(credentials.get("email"));
            String password = credentials.get("password");
            String ipAddress = getClientIp(request);

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email y contraseña son requeridos"
                ));
            }

            Map<String, Object> result = userService.login(email, password, ipAddress);

            if (Boolean.TRUE.equals(result.get("success"))) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(401).body(result);
            }
        } catch (Exception e) {
            logger.error("Error en el proceso de login", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor durante el login"
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getRolUsuario(@RequestParam Integer id) {
        try {
            return userService.getUserById(id)
                    .map(user -> ResponseEntity.ok(Map.of("rol", user.getRol().toString())))
                    .orElse(ResponseEntity.status(404).body(Map.of("message", "Usuario no encontrado")));
        } catch (Exception e) {
            logger.error("Error al obtener rol de usuario", e);
            return ResponseEntity.status(500).body(Map.of("message", "Error interno del servidor"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            String email = sanitizeEmail(body.get("email"));
            String ipAddress = getClientIp(request);

            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email es requerido"
                ));
            }

            Optional<User> userOptional = userService.getUserByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String newPassword = generateTemporaryPassword();

                try {
                    userService.forzarCambioPassword(user.getId_usuario(), newPassword, ipAddress);

                    // Enviar notificación
                    notificacionClient.enviarNotificacionResetPassword(email, user.getNombre(), newPassword);

                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Se ha enviado un correo con la nueva contraseña."
                    ));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", e.getMessage()
                    ));
                }
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Usuario no encontrado con ese correo."
                ));
            }
        } catch (Exception e) {
            logger.error("Error al resetear contraseña", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    @PostMapping("/desbloquear/{id}")
    public ResponseEntity<?> desbloquearUsuario(@PathVariable Integer id) {
        try {
            userService.desbloquearUsuario(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario desbloqueado correctamente"
            ));
        } catch (Exception e) {
            logger.error("Error al desbloquear usuario", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    @PostMapping("/forzar-cambio-password/{id}")
    public ResponseEntity<?> forzarCambioPassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        try {
            String nuevaPassword = request.get("nuevaPassword");
            String ipAddress = getClientIp(httpRequest);

            if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "La nueva contraseña es requerida"
                ));
            }

            boolean success = userService.forzarCambioPassword(id, nuevaPassword, ipAddress);

            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contraseña cambiada exitosamente"
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "Usuario no encontrado"
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error al forzar cambio de contraseña", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }

    @GetMapping("/password-policy")
    public ResponseEntity<?> getPasswordPolicy() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "requirements", passwordPolicyService.getPasswordRequirements(),
            "minLength", 12,
            "requiresUppercase", true,
            "requiresLowercase", true,
            "requiresNumbers", true,
            "requiresSpecial", true
        ));
    }

    @GetMapping("/activos")
    public ResponseEntity<?> getUsuariosActivos() {
        try {
            List<User> usuarios = userService.getUsuariosActivos();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "usuarios", usuarios
            ));
        } catch (Exception e) {
            logger.error("Error al obtener usuarios activos", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    @PutMapping("/desactivar/{id}")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Integer id) {
        try {
            userService.desactivarUsuario(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario desactivado correctamente."
            ));
        } catch (Exception e) {
            logger.error("Error al desactivar usuario", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    @GetMapping("/rol/{rol}")
    public ResponseEntity<?> getUsuariosPorRol(@PathVariable Rol rol) {
        try {
            List<User> usuarios = userService.getUsuariosPorRol(rol);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "usuarios", usuarios
            ));
        } catch (Exception e) {
            logger.error("Error al obtener usuarios por rol", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    @PutMapping("/activar/{id}")
    public ResponseEntity<?> activarUsuario(@PathVariable Integer id) {
        try {
            userService.activarUsuario(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario activado y estado cambiado a APROBADO correctamente."
            ));
        } catch (Exception e) {
            logger.error("Error al activar usuario", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    private String generateTemporaryPassword() {
        // Generar contraseña temporal que cumpla las políticas
        return "Temp123!";
    }

    @PostMapping("/debug-login")
    public ResponseEntity<?> debugLogin(@RequestBody Map<String, String> credentials, @Autowired PasswordEncoder passwordEncoder) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            logger.info("=== DEBUG LOGIN ===");
            logger.info("Email: {}", email);

            Optional<User> userOptional = userService.getUserByEmail(email);

            if (userOptional.isEmpty()) {
                logger.warn("❌ Usuario no encontrado");
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Usuario no encontrado",
                    "debug", "No existe usuario con ese email"
                ));
            }

            User user = userOptional.get();
            logger.info("✅ Usuario encontrado: {}", user.getEmail());
            logger.info("📝 Estado: {}", user.getEstado());
            logger.info("🔓 Activo: {}", user.isActivo());
            logger.info("🔐 Contraseña en DB: {}", user.getPassword());
            logger.info("🔑 Longitud password: {}", user.getPassword() != null ? user.getPassword().length() : "null");

            // Verificar contraseña
            boolean passwordMatch = false;
            try {
                passwordMatch = passwordEncoder.matches(password, user.getPassword());
                logger.info("🔍 ¿Coincide contraseña?: {}", passwordMatch);
            } catch (Exception e) {
                logger.error("❌ Error al verificar contraseña: {}", e.getMessage());
                return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno",
                    "debug", e.getMessage()
                ));
            }

            if (!EstadoUsuario.APROBADO.equals(user.getEstado())) {
                logger.warn("❌ Usuario no aprobado");
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Usuario no aprobado",
                    "debug", "Estado actual: " + user.getEstado()
                ));
            }

            if (!user.isActivo()) {
                logger.warn("❌ Usuario inactivo");
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Usuario inactivo",
                    "debug", "Usuario marcado como inactivo"
                ));
            }

            if (passwordMatch) {
                logger.info("🎉 Login exitoso");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login exitoso (debug)",
                    "debug", "Todo correcto"
                ));
            } else {
                logger.warn("❌ Contraseña incorrecta");
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Contraseña incorrecta",
                    "debug", "La contraseña no coincide"
                ));
            }

        } catch (Exception e) {
            logger.error("💥 Error en debug: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno",
                "debug", e.getMessage()
            ));
        }
    }

    @GetMapping("/test-cors")
    public ResponseEntity<?> testCors() {
        logger.info("=== TEST CORS ENDPOINT LLAMADO ===");
        return ResponseEntity.ok(Map.of(
            "message", "CORS test exitoso",
            "timestamp", System.currentTimeMillis()
        ));
    }
}