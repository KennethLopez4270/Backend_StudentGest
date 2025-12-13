package com.studentgest.user_service.controller;

import com.studentgest.user_service.service.EmailValidationService;
import com.studentgest.user_service.service.SecurityConfigService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/email")
public class EmailValidationController {
    
    @Autowired
    private EmailValidationService emailValidationService;
    
    @Autowired 
    private SecurityConfigService securityConfigService;
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "valid", false,
                    "message", "Email es requerido"
                ));
            }
            
            boolean isValid = emailValidationService.isValidEmail(email);
            String requirements = emailValidationService.getEmailRequirements();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "valid", isValid,
                "requirements", requirements
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "valid", false,
                "message", "Error validando email"
            ));
        }
    }
    @GetMapping("/allowed-domains")
    public ResponseEntity<?> getAllowedDomains() {
        try {
            Map<String, Object> emailConfig = securityConfigService.getEmailConfig();
            String allowedDomains = (String) emailConfig.get("allowedDomains");
            
            List<String> domains = Arrays.asList(allowedDomains.split(","));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "domains", domains
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error obteniendo dominios permitidos"
            ));
        }
    }
    @GetMapping("/debug-config")
    public ResponseEntity<?> debugEmailConfig() {
        try {
            Map<String, Object> emailConfig = securityConfigService.getEmailConfig(); 
            
            // Probar el email específico
            String testEmail = "alejandromollinedorodriguez@gmail.com";
            String emailPattern = (String) emailConfig.get("emailPattern");
            String allowedDomains = (String) emailConfig.get("allowedDomains");
            
            boolean patternMatch = Pattern.matches(emailPattern, testEmail);
            String domain = testEmail.substring(testEmail.indexOf('@') + 1);
            List<String> permittedDomains = Arrays.asList(allowedDomains.split(","));
            boolean domainMatch = permittedDomains.stream().anyMatch(domain::endsWith);
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("emailPattern", emailPattern);
            debugInfo.put("allowedDomains", allowedDomains);
            debugInfo.put("testEmail", testEmail);
            debugInfo.put("patternMatch", patternMatch);
            debugInfo.put("extractedDomain", domain);
            debugInfo.put("domainMatch", domainMatch);
            debugInfo.put("permittedDomainsList", permittedDomains);
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}