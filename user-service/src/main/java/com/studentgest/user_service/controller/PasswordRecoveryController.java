package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.UserRepository;
import com.studentgest.user_service.service.AuditLogService;
import com.studentgest.user_service.service.EmailService;
import com.studentgest.user_service.service.PasswordHistoryService;
import com.studentgest.user_service.service.PasswordPolicyService;
import com.studentgest.user_service.service.SecurityConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/password-recovery")
public class PasswordRecoveryController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private PasswordHistoryService passwordHistoryService;

    @Autowired
    private SecurityConfigService securityConfigService;

    @Autowired
    private AuditLogService auditLogService;

    // Solicitar recuperación de contraseña
    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordRecovery(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Email es requerido");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase().trim());

            if (userOptional.isEmpty()) {
                // Por seguridad, no revelar si el email existe o no
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Si el email existe, se ha enviado un enlace de recuperación");
                return ResponseEntity.ok(response);
            }

            User user = userOptional.get();

            // Generar token de recuperación
            String recoveryToken = UUID.randomUUID().toString();

            // Guardar token en la base de datos
            user.setTokenVerificacion(recoveryToken);
            user.setExpiracionTokenVerificacion(
                    Timestamp.valueOf(LocalDateTime.now().plusHours(24)) // 24 horas de expiración
            );

            userRepository.save(user);

            // ✅ LOG DE SEGURIDAD: Solicitud de recuperación de contraseña
            auditLogService.logPasswordRecoveryRequest(email, "N/A");

            // Enviar email con enlace de recuperación
            String recoveryLink = "http://localhost:3000/restablecer-contrasena?token=" + recoveryToken;

            boolean emailSent = emailService.sendPasswordRecoveryEmail(
                    user.getEmail(),
                    user.getNombre(),
                    recoveryLink);

            Map<String, Object> response = new HashMap<>();
            if (emailSent) {
                response.put("success", true);
                response.put("message", "Se ha enviado un enlace de recuperación a tu email");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Error al enviar el email de recuperación");
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            logger.error("Error en solicitud de recuperación de contraseña", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Verificar token de recuperación
    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyRecoveryToken(@RequestParam String token) {
        try {
            Optional<User> userOptional = userRepository.findByTokenVerificacion(token);

            Map<String, Object> response = new HashMap<>();
            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "Token inválido o expirado");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOptional.get();

            // Verificar expiración
            if (user.getExpiracionTokenVerificacion().before(new Timestamp(System.currentTimeMillis()))) {
                response.put("success", false);
                response.put("message", "El token ha expirado");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("message", "Token válido");
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error verificando token de recuperación", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Restablecer contraseña con token
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            Map<String, Object> response = new HashMap<>();

            if (token == null || newPassword == null || confirmPassword == null) {
                response.put("success", false);
                response.put("message", "Todos los campos son requeridos");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "Las contraseñas no coinciden");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar política de contraseñas
            if (!passwordPolicyService.validatePassword(newPassword)) {
                response.put("success", false);
                response.put("message", "La contraseña no cumple con las políticas de seguridad");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<User> userOptional = userRepository.findByTokenVerificacion(token);

            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "Token inválido o expirado");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOptional.get();

            // Verificar expiración
            if (user.getExpiracionTokenVerificacion().before(new Timestamp(System.currentTimeMillis()))) {
                // ✅ LOG DE SEGURIDAD: Intento de reset con token expirado
                auditLogService.logPasswordRecoveryReset(user.getEmail(), false, "N/A");
                response.put("success", false);
                response.put("message", "El token ha expirado");
                return ResponseEntity.badRequest().body(response);
            }

            String newPasswordHash = passwordEncoder.encode(newPassword);

            // Verificar que no esté en el historial
            if (passwordHistoryService.isPasswordInHistory(user.getId_usuario(), newPassword)) {
                Map<String, Object> config = securityConfigService.loadSecurityConfig();
                int historySize = (Integer) config.get("passwordHistorySize");
                response.put("success", false);
                response.put("message", "No puede reutilizar las últimas " + historySize + " contraseñas");
                return ResponseEntity.badRequest().body(response);
            }

            // Guardar contraseña actual en historial
            passwordHistoryService.addToPasswordHistory(user.getId_usuario(), user.getPassword());

            // Actualizar contraseña
            user.setPassword(newPasswordHash);
            user.setTokenVerificacion(null); // Limpiar token
            user.setExpiracionTokenVerificacion(null);
            user.setUltimoCambioPassword(new Timestamp(System.currentTimeMillis()));
            user.setIntentosFallidos(0);
            user.setBloqueado(false);

            // Establecer nueva expiración
            Map<String, Object> config = securityConfigService.loadSecurityConfig();
            int expiryDays = (Integer) config.get("passwordExpiryDays");
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(expiryDays);
            user.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));

            userRepository.save(user);

            // ✅ LOG DE SEGURIDAD: Restablecimiento de contraseña exitoso
            auditLogService.logPasswordRecoveryReset(user.getEmail(), true, "N/A");

            // Enviar notificación de cambio de contraseña
            emailService.sendPasswordChangedNotification(user.getEmail(), user.getNombre());

            response.put("success", true);
            response.put("message", "Contraseña restablecida exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error restableciendo contraseña", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/find-user")
    public ResponseEntity<?> findUserByEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Email es requerido");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase().trim());

            Map<String, Object> response = new HashMap<>();
            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "No se encontró un usuario con ese email");
                return ResponseEntity.ok(response);
            }

            User user = userOptional.get();

            // Retornar información básica del usuario (sin datos sensibles)
            response.put("success", true);
            response.put("message", "Usuario encontrado");
            response.put("user", Map.of(
                    "id", user.getId_usuario(),
                    "email", user.getEmail(),
                    "nombre", user.getNombre()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error buscando usuario", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Cambiar contraseña por email (sin token)
    @PostMapping("/change-by-email")
    public ResponseEntity<?> changePasswordByEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            Map<String, Object> response = new HashMap<>();

            if (email == null || newPassword == null || confirmPassword == null) {
                response.put("success", false);
                response.put("message", "Todos los campos son requeridos");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "Las contraseñas no coinciden");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar política de contraseñas
            if (!passwordPolicyService.validatePassword(newPassword)) {
                response.put("success", false);
                response.put("message", "La contraseña no cumple con las políticas de seguridad");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase().trim());

            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOptional.get();
            String newPasswordHash = passwordEncoder.encode(newPassword);

            // Verificar que no esté en el historial
            if (passwordHistoryService.isPasswordInHistory(user.getId_usuario(), newPassword)) {
                Map<String, Object> config = securityConfigService.loadSecurityConfig();
                int historySize = (Integer) config.get("passwordHistorySize");
                response.put("success", false);
                response.put("message", "No puede reutilizar las últimas " + historySize + " contraseñas");
                return ResponseEntity.badRequest().body(response);
            }

            // Guardar contraseña actual en historial
            passwordHistoryService.addToPasswordHistory(user.getId_usuario(), user.getPassword());

            // Actualizar contraseña
            user.setPassword(newPasswordHash);
            user.setUltimoCambioPassword(new Timestamp(System.currentTimeMillis()));
            user.setIntentosFallidos(0);
            user.setBloqueado(false);

            // Establecer nueva expiración
            Map<String, Object> config = securityConfigService.loadSecurityConfig();
            int expiryDays = (Integer) config.get("passwordExpiryDays");
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(expiryDays);
            user.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));

            userRepository.save(user);

            // ✅ LOG DE SEGURIDAD: Cambio de contraseña por email exitoso
            auditLogService.logPasswordRecoveryReset(email, true, "N/A");

            // Enviar notificación de cambio de contraseña
            emailService.sendPasswordChangedNotification(user.getEmail(), user.getNombre());

            response.put("success", true);
            response.put("message", "Contraseña cambiada exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error cambiando contraseña por email", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(500).body(response);
        }
    }
}