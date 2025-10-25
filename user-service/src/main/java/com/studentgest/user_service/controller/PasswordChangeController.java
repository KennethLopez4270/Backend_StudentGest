package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.PasswordHistory;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.PasswordHistoryRepository;
import com.studentgest.user_service.repository.UserRepository;
import com.studentgest.user_service.service.AuditLogService;
import com.studentgest.user_service.service.EmailService;
import com.studentgest.user_service.service.PasswordHistoryService;
import com.studentgest.user_service.service.PasswordPolicyService;
import com.studentgest.user_service.service.SecurityConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/password-change")
public class PasswordChangeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

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
                response.put("success", false);
                response.put("message", "La contraseña actual es incorrecta");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar nueva contraseña
            if (!passwordPolicyService.validatePassword(newPassword)) {
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

            // ✅ CORREGIDO: Usar el método del servicio para verificar historial
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

        // Registrar en auditoría
        auditLogService.logPasswordChange(user.getId_usuario(), "Cambio voluntario");

        // Enviar notificación
        emailService.sendPasswordChangedNotification(user.getEmail(), user.getNombre());

        response.put("success", true);
        response.put("message", "Contraseña cambiada exitosamente");
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Error interno del servidor");
        return ResponseEntity.status(500).body(response);
    }
}

@PostMapping("/check-history")
public ResponseEntity<?> checkPasswordHistory(@RequestBody Map<String, String> request, Authentication authentication) {
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
        
        // ✅ CORREGIDO: Usar el método del servicio
        boolean isInHistory = passwordHistoryService.isPasswordInHistory(user.getId_usuario(), password);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isInHistory", isInHistory);
        response.put("message", isInHistory ? 
            "Esta contraseña ha sido utilizada anteriormente" : 
            "Contraseña válida (no está en el historial)");
        
        return ResponseEntity.ok(response);

    } catch (Exception e) {
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
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Error obteniendo política de contraseñas");
        return ResponseEntity.status(500).body(response);
    }
}
}