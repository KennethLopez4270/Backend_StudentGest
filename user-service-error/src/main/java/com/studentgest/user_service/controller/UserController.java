package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.service.PasswordPolicyService;
import com.studentgest.user_service.service.SecurityConfigService;
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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private SecurityConfigService securityConfigService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            List<User> users = userService.obtenerTodos();
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
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.obtenerPorId(id);
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
            logger.info("IdRol: {}", user.getIdRol());
            logger.info("Password length: {}", user.getPassword() != null ? user.getPassword().length() : "null");

            // Sanitizar y normalizar inputs
            user.setNombre(sanitizeInput(user.getNombre()));
            user.setApellido_paterno(sanitizeInput(user.getApellido_paterno()));
            user.setApellido_materno(sanitizeInput(user.getApellido_materno()));
            user.setEmail(sanitizeEmail(user.getEmail()));
            user.setDocumentoIdentidad(sanitizeInput(user.getDocumentoIdentidad()));

            logger.info("✅ Datos sanitizados, llamando a userService...");

            User savedUser = userService.crearUsuario(user);

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
                            "idRol", savedUser.getIdRol(),
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
                    "message", "El email o documento de identidad ya está en uso"
            ));
        } catch (Exception e) {
            logger.error("💥 ERROR INTERNO: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Error interno del servidor",
                    "message", e.getMessage() != null ? e.getMessage() : "Error desconocido",
                    "details", e.getClass().getName()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody @Valid User user) {
        try {
            // Sanitizar inputs
            user.setNombre(sanitizeInput(user.getNombre()));
            user.setApellido_paterno(sanitizeInput(user.getApellido_paterno()));
            user.setApellido_materno(sanitizeInput(user.getApellido_materno()));
            user.setEmail(sanitizeEmail(user.getEmail()));
            user.setDocumentoIdentidad(sanitizeInput(user.getDocumentoIdentidad()));

            User updatedUser = userService.actualizarUsuario(id, user);
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
        } catch (NoSuchElementException e) {
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

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email y contraseña son requeridos"
                ));
            }

            Map<String, Object> result = userService.login(email, password);
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

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            String identificador = sanitizeInput(body.get("identificador"));
            String ipAddress = getClientIp(request);

            if (identificador == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Identificador (email o documento) es requerido"
                ));
            }

            String newPassword = generateTemporaryPassword();
            userService.resetPassword(identificador, newPassword);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Se ha enviado un correo con la nueva contraseña."
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Usuario no encontrado con ese identificador."
            ));
        } catch (Exception e) {
            logger.error("Error al resetear contraseña", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    @PostMapping("/desbloquear/{id}")
    public ResponseEntity<?> desbloquearUsuario(@PathVariable Long id) {
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
            @PathVariable Long id,
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

            userService.resetPassword(String.valueOf(id), nuevaPassword);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contraseña cambiada exitosamente"
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "Usuario no encontrado"
            ));
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
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "requirements", passwordPolicyService.getPasswordRequirements(),
                    "minLength", 12,
                    "requiresUppercase", true,
                    "requiresLowercase", true,
                    "requiresNumbers", true,
                    "requiresSpecial", true
            ));
        } catch (Exception e) {
            logger.error("Error al obtener política de contraseñas", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    @GetMapping("/simple-policy")
    public ResponseEntity<?> getSimplePolicy() {
        System.out.println("🎯 Endpoint /simple-policy llamado");
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Endpoint funcionando",
                "minLength", 6,
                "requiresUppercase", true,
                "requiresLowercase", true,
                "requiresNumbers", true,
                "requiresSpecial", true,
                "allowedSpecialChars", "@$!%*?&"
        ));
    }

    @GetMapping("/debug/password-policy")
    public ResponseEntity<?> debugPasswordPolicy() {
        try {
            Map<String, Object> config = securityConfigService.loadSecurityConfig();
            Integer minLength = securityConfigService.getIntegerValue("LONGITUD_MINIMA_CONTRASENA", -1);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "debug_info", Map.of(
                            "minLength_from_bd", minLength,
                            "config_loaded", config,
                            "table_exists", true
                    ),
                    "policy", Map.of(
                            "minLength", config.get("minPasswordLength"),
                            "requiresUppercase", config.get("requiresUppercase"),
                            "requiresLowercase", config.get("requiresLowercase"),
                            "requiresNumbers", config.get("requiresNumbers"),
                            "requiresSpecial", config.get("requiresSpecial"),
                            "allowedSpecialChars", config.get("allowedSpecialChars")
                    )
            ));
        } catch (Exception e) {
            logger.error("Error en debug password policy", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/public/password-policy")
    public ResponseEntity<?> getPublicPasswordPolicy() {
        try {
            Map<String, Object> config = securityConfigService.loadSecurityConfig();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "requirements", passwordPolicyService.getPasswordRequirements(),
                    "minLength", config.get("minPasswordLength"),
                    "requiresUppercase", config.get("requiresUppercase"),
                    "requiresLowercase", config.get("requiresLowercase"),
                    "requiresNumbers", config.get("requiresNumbers"),
                    "requiresSpecial", config.get("requiresSpecial"),
                    "allowedSpecialChars", config.get("allowedSpecialChars")
            ));
        } catch (Exception e) {
            logger.error("Error al obtener política de contraseñas pública", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<?> getUsuariosActivos() {
        try {
            List<User> usuarios = userService.obtenerUsuariosActivos();
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
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long id) {
        try {
            userService.desactivarUsuario(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Usuario desactivado correctamente."
            ));
        } catch (NoSuchElementException e) {
            logger.error("Usuario no encontrado para desactivar", e);
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Usuario no encontrado"
            ));
        } catch (Exception e) {
            logger.error("Error al desactivar usuario", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    @GetMapping("/rol/{idRol}")
    public ResponseEntity<?> getUsuariosPorRol(@PathVariable Long idRol) {
        try {
            List<User> usuarios = userService.obtenerPorRol(idRol);
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
    public ResponseEntity<?> activarUsuario(@PathVariable Long id) {
        try {
            userService.activarUsuario(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Usuario activado y estado cambiado a APROBADO correctamente."
            ));
        } catch (NoSuchElementException e) {
            logger.error("Usuario no encontrado para activar", e);
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Usuario no encontrado"
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
        return "Temp123!";
    }

    @PostMapping("/debug-login")
    public ResponseEntity<?> debugLogin(@RequestBody Map<String, String> credentials) {
        try {
            String email = sanitizeEmail(credentials.get("email"));
            String password = credentials.get("password");

            logger.info("=== DEBUG LOGIN ===");
            logger.info("Email: {}", email);

            Optional<User> userOptional = userService.obtenerPorEmail(email);

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

            boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
            logger.info("🔍 ¿Coincide contraseña?: {}", passwordMatch);

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