package com.studentgest.user_service.controller;

import com.studentgest.user_service.service.AdvancedPasswordPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password-strength")
public class PasswordStrengthController {
    
    @Autowired
    private AdvancedPasswordPolicyService advancedPasswordPolicyService;
    
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluatePasswordStrength(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            
            if (password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Password es requerido"
                ));
            }
            
            AdvancedPasswordPolicyService.PasswordStrengthResult result = 
                advancedPasswordPolicyService.evaluatePasswordStrength(password);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "score", result.getScore(),
                "strength", result.getStrength(),
                "feedback", result.getFeedback()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error evaluando fortaleza de contraseña"
            ));
        }
    }
}