package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.SecurityPolicy;
import com.studentgest.user_service.service.AuditLogService;
import com.studentgest.user_service.service.SecurityConfigService;
import com.studentgest.user_service.service.PasswordPolicyService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security-config")
public class SecurityConfigController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigController.class);

    @Autowired
    private SecurityConfigService securityConfigService;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private AuditLogService auditLogService;

    // Endpoint para obtener todas las configuraciones
    @GetMapping("/all")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<?> getAllConfigurations(HttpServletRequest request) {
        try {
            String ipAddress = getClientIp(request);
            // ✅ LOG DE SEGURIDAD: Acceso a configuración de seguridad
            auditLogService.logSecurityConfigAccess(null, ipAddress);

            Map<String, Map<String, Object>> configs = securityConfigService.getAllConfigurations();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "configurations", configs));
        } catch (Exception e) {
            logger.error("Error al obtener configuraciones", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error obteniendo configuraciones"));
        }
    }

    // Los demás métodos existentes se mantienen igual...
    @GetMapping
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<?> getAllConfig(HttpServletRequest request) {
        try {
            String ipAddress = getClientIp(request);
            // ✅ LOG DE SEGURIDAD: Acceso a configuración de seguridad
            auditLogService.logSecurityConfigAccess(null, ipAddress);

            Map<String, Object> config = securityConfigService.loadSecurityConfig();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "config", config));
        } catch (Exception e) {
            logger.error("Error al obtener configuración de seguridad", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"));
        }
    }

    @GetMapping("/category-policies/{category}")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<?> getConfigByCategory(@PathVariable String category) {
        try {
            List<SecurityPolicy> policies = securityConfigService.getPoliciesByCategory(category);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "policies", policies));
        } catch (Exception e) {
            logger.error("Error al obtener políticas por categoría", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"));
        }
    }

    @PutMapping("/{policyName}")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<?> updatePolicy(
            @PathVariable String policyName,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        try {
            String newValue = request.get("value");
            String ipAddress = getClientIp(httpRequest);

            if (newValue == null || newValue.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "El valor de la política es requerido"));
            }

            boolean updated = securityConfigService.updatePolicy(policyName, newValue.trim());

            if (updated) {
                // ✅ LOG DE SEGURIDAD: Cambio de configuración de seguridad
                auditLogService.logConfigurationChange(policyName, "N/A", newValue.trim(), null, ipAddress);
                logger.info("Política actualizada: {} = {}", policyName, newValue);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Política actualizada correctamente"));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "No se pudo actualizar la política. Puede que no exista o no sea editable"));
            }

        } catch (Exception e) {
            logger.error("Error al actualizar política: {}", policyName, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"));
        }
    }

    @GetMapping("/password-policy")
    public ResponseEntity<?> getPasswordPolicy() {
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
                    "allowedSpecialChars", config.get("allowedSpecialChars")));
        } catch (Exception e) {
            logger.error("Error al obtener política de contraseñas", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"));
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