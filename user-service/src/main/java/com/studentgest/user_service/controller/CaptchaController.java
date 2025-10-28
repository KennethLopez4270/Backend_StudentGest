package com.studentgest.user_service.controller;

import com.studentgest.user_service.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    
    @Autowired
    private CaptchaService captchaService;
    
    @GetMapping("/config")
    public ResponseEntity<?> getCaptchaConfig() {
        try {
            boolean isEnabled = captchaService.isCaptchaEnabled();
            String siteKey = captchaService.getRecaptchaSiteKey();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "enabled", isEnabled,
                "siteKey", siteKey
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error obteniendo configuración del CAPTCHA"
            ));
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCaptcha(@RequestBody Map<String, String> request) {
        try {
            String recaptchaResponse = request.get("recaptchaResponse");
            String clientIp = request.get("clientIp");
            
            if (recaptchaResponse == null || recaptchaResponse.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "valid", false,
                    "message", "Token reCAPTCHA es requerido"
                ));
            }
            
            boolean isValid = captchaService.verifyRecaptcha(recaptchaResponse, clientIp);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "valid", isValid
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "valid", false,
                "message", "Error validando CAPTCHA: " + e.getMessage()
            ));
        }
    }
}