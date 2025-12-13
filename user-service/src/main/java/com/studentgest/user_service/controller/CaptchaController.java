package com.studentgest.user_service.controller;

import com.studentgest.user_service.service.AuditLogService;
import com.studentgest.user_service.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/config")
    public ResponseEntity<?> getCaptchaConfig() {
        try {
            boolean isEnabled = captchaService.isCaptchaEnabled();
            String siteKey = captchaService.getRecaptchaSiteKey();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "enabled", isEnabled,
                    "siteKey", siteKey));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error obteniendo configuración del CAPTCHA"));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCaptcha(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            String recaptchaResponse = request.get("recaptchaResponse");
            String clientIp = request.get("clientIp");
            String ipAddress = getClientIp(httpRequest);

            if (recaptchaResponse == null || recaptchaResponse.trim().isEmpty()) {
                // ✅ LOG DE SEGURIDAD: CAPTCHA vacío
                auditLogService.logCaptchaValidation(ipAddress, false);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "valid", false,
                        "message", "Token reCAPTCHA es requerido"));
            }

            boolean isValid = captchaService.verifyRecaptcha(recaptchaResponse, clientIp);

            // ✅ LOG DE SEGURIDAD: Resultado de validación CAPTCHA
            auditLogService.logCaptchaValidation(ipAddress, isValid);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "valid", isValid));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "valid", false,
                    "message", "Error validando CAPTCHA: " + e.getMessage()));
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