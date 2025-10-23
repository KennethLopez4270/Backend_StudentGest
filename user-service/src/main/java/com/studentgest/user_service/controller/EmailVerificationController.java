package com.studentgest.user_service.controller;

import com.studentgest.user_service.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email-verification")
public class EmailVerificationController {
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        boolean success = emailVerificationService.verifyEmail(token);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verificado exitosamente"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Token de verificación inválido o expirado"
            ));
        }
    }
    
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email es requerido"
            ));
        }
        
        boolean success = emailVerificationService.resendVerificationEmail(email);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email de verificación reenviado exitosamente"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al reenviar email de verificación"
            ));
        }
    }
    
    @PostMapping("/clean-expired-tokens")
    public ResponseEntity<?> cleanExpiredTokens() {
        try {
            emailVerificationService.cleanExpiredVerificationTokens();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tokens expirados limpiados exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error limpiando tokens expirados"
            ));
        }
    }
}