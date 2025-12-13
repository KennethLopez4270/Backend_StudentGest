package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.UserRepository;
import com.studentgest.user_service.service.AuditLogService;
import com.studentgest.user_service.service.EmailService;
import com.studentgest.user_service.service.PasswordHistoryService;
import com.studentgest.user_service.service.PasswordPolicyService;
import com.studentgest.user_service.service.SecurityConfigService;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/password-change")
public class PasswordChangeController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordChangeController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private PasswordHistoryService passwordHistoryService;

    @Autowired
    private SecurityConfigService securityConfigService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            Map<String, Object> response = new HashMap<>();

            if (currentPassword == null || newPassword == null || confirmPassword == null) {
                response.put("success", false);
                response.put("message", "Todos los campos son requeridos");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "Las nuevas contraseñas no coinciden");
                return ResponseEntity.badRequest().body(response);
            }

            String email = authentication.getName();
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOptional.get();

            // Verificar contraseña actual
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                // ✅ LOG DE SEGURIDAD: Cambio de contraseña fallido
                auditLogService.logPasswordChangeAttempt(user.getId_usuario(), false, "Contraseña actual incorrecta",
                        "N/A");
                response.put("success", false);
                response.put("message", "La contraseña actual es incorrecta");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar nueva contraseña
            if (!passwordPolicyService.validatePassword(newPassword)) {
                // ✅ LOG DE SEGURIDAD: Cambio de contraseña fallido
                auditLogService.logPasswordChangeAttempt(user.getId_usuario(), false, "No cumple política de seguridad",
                        "N/A");
                response.put("success", false);
                response.put("message", passwordPolicyService.getPasswordRequirements());
                return ResponseEntity.badRequest().body(response);
            }

            // Verificar que no sea igual a la actual
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                response.put("success", false);
                response.put("message", "La nueva contraseña debe ser diferente a la actual");
                return ResponseEntity.badRequest().body(response);
            }

            // Verificar historial
            boolean isInHistory = passwordHistoryService.isPasswordInHistory(user.getId_usuario(), newPassword);

            if (isInHistory) {
                Map<String, Object> config = securityConfigService.loadSecurityConfig();
                int historySize = (Integer) config.get("passwordHistorySize");
                response.put("success", false);
                response.put("message", "No puede reutilizar las últimas " + historySize + " contraseñas");
                return ResponseEntity.badRequest().body(response);
            }

            // Guardar contraseña actual en historial ANTES de cambiarla
            passwordHistoryService.addToPasswordHistory(user.getId_usuario(), user.getPassword());

            // Actualizar contraseña
            String newPasswordHash = passwordEncoder.encode(newPassword);
            user.setPassword(newPasswordHash);
            user.setUltimoCambioPassword(new Timestamp(System.currentTimeMillis()));
            user.setRequiresPasswordChange(false);

            // Establecer nueva expiración
            Map<String, Object> config = securityConfigService.loadSecurityConfig();
            int expiryDays = (Integer) config.get("passwordExpiryDays");
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(expiryDays);
            user.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));

            userRepository.save(user);

            // ✅ LOG DE SEGURIDAD: Cambio de contraseña exitoso
            auditLogService.logPasswordChangeAttempt(user.getId_usuario(), true, "Cambio voluntario exitoso", "N/A");

            // Enviar notificación
            emailService.sendPasswordChangedNotification(user.getEmail(), user.getNombre());

            response.put("success", true);
            response.put("message", "Contraseña cambiada exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error en cambio de contraseña", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/check-history")
    public ResponseEntity<?> checkPasswordHistory(@RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String password = request.get("password");

            if (password == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Password es requerido");
                return ResponseEntity.badRequest().body(response);
            }

            String email = authentication.getName();
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOptional.get();

            boolean isInHistory = passwordHistoryService.isPasswordInHistory(user.getId_usuario(), password);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isInHistory", isInHistory);
            response.put("message", isInHistory ? "Esta contraseña ha sido utilizada anteriormente"
                    : "Contraseña válida (no está en el historial)");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error verificando historial de contraseñas", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error verificando historial de contraseñas");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/policy")
    public ResponseEntity<?> getPasswordPolicy() {
        try {
            Map<String, Object> config = securityConfigService.loadSecurityConfig();

            Map<String, Object> policy = new HashMap<>();
            policy.put("minLength", config.get("minPasswordLength"));
            policy.put("requiresUppercase", config.get("requiresUppercase"));
            policy.put("requiresLowercase", config.get("requiresLowercase"));
            policy.put("requiresNumbers", config.get("requiresNumbers"));
            policy.put("requiresSpecial", config.get("requiresSpecial"));
            policy.put("allowedSpecialChars", config.get("allowedSpecialChars"));
            policy.put("passwordHistorySize", config.get("passwordHistorySize"));
            policy.put("passwordExpiryDays", config.get("passwordExpiryDays"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("policy", policy);
            response.put("requirements", passwordPolicyService.getPasswordRequirements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo política de contraseñas", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error obteniendo política de contraseñas");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/forced-change")
    public ResponseEntity<?> forcedPasswordChange(@RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            String email = request.get("email");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");
            String ipAddress = getClientIp(httpRequest);

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
            user.setRequiresPasswordChange(false);

            // Actualizar fecha de expiración
            Map<String, Object> config = securityConfigService.loadSecurityConfig();
            int expiryDays = (Integer) config.get("passwordExpiryDays");
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(expiryDays);
            user.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));

            userRepository.save(user);

            // ✅ LOG DE SEGURIDAD: Cambio forzado de contraseña exitoso
            auditLogService.logPasswordChange(user.getId_usuario(), ipAddress);

            // Enviar notificación de cambio de contraseña
            emailService.sendPasswordChangedNotification(user.getEmail(), user.getNombre());

            response.put("success", true);
            response.put("message", "Contraseña cambiada exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error en cambio forzado de contraseña", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(500).body(response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}