package com.studentgest.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class EmailValidationService {
    
    @Autowired
    private SecurityConfigService securityConfigService;
    
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("❌ Email es null o vacío");
            return false;
        }
        
        email = email.trim().toLowerCase();
        System.out.println("📧 Validando email: " + email);
        
        Map<String, Object> emailConfig = securityConfigService.getEmailConfig();
        String emailPattern = (String) emailConfig.get("emailPattern");
        String allowedDomains = (String) emailConfig.get("allowedDomains");
        
        System.out.println("🔧 Patrón regex: " + emailPattern);
        System.out.println("🔧 Dominios permitidos: " + allowedDomains);
        
        // Validar formato básico
        boolean patternMatches = Pattern.matches(emailPattern, email);
        System.out.println("✅ Validación regex: " + patternMatches);
        
        if (!patternMatches) {
            System.out.println("❌ Email no cumple el patrón regex");
            return false;
        }
        
        // Validar dominio permitido
        String domain = email.substring(email.indexOf('@') + 1);
        System.out.println("🌐 Dominio extraído: " + domain);
        
        List<String> permittedDomains = Arrays.asList(allowedDomains.split(","));
        boolean domainValid = permittedDomains.stream().anyMatch(domain::endsWith);
        
        System.out.println("✅ Validación dominio: " + domainValid);
        System.out.println("📧 Email " + email + " es válido: " + domainValid);
        
        return domainValid;
    }
    
    public String getEmailRequirements() {
        Map<String, Object> emailConfig = securityConfigService.getEmailConfig();
        String allowedDomains = (String) emailConfig.get("allowedDomains");
        return "Debe usar un dominio válido: " + allowedDomains;
    }
    
    public boolean isEmailVerificationRequired() {
        Map<String, Object> emailConfig = securityConfigService.getEmailConfig();
        return (Boolean) emailConfig.get("emailVerificationRequired");
    }
}