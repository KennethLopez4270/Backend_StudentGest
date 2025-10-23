package com.studentgest.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class PasswordPolicyService {
    
    @Autowired
    private SecurityConfigService securityConfigService;
    
    public boolean validatePassword(String password) {
        if (password == null) return false;
        
        Map<String, Object> config = securityConfigService.loadSecurityConfig();
        
        int minLength = (Integer) config.get("minPasswordLength");
        boolean requiresUppercase = (Boolean) config.get("requiresUppercase");
        boolean requiresLowercase = (Boolean) config.get("requiresLowercase");
        boolean requiresNumbers = (Boolean) config.get("requiresNumbers");
        boolean requiresSpecial = (Boolean) config.get("requiresSpecial");
        String specialChars = (String) config.get("allowedSpecialChars");
        
        if (password.length() < minLength) {
            return false;
        }
        
        boolean hasUppercase = !requiresUppercase || password.matches(".*[A-Z].*");
        boolean hasLowercase = !requiresLowercase || password.matches(".*[a-z].*");
        boolean hasNumbers = !requiresNumbers || password.matches(".*[0-9].*");
        boolean hasSpecial = !requiresSpecial || password.matches(".*[" + Pattern.quote(specialChars) + "].*");
        
        return hasUppercase && hasLowercase && hasNumbers && hasSpecial;
    }
    
    public String getPasswordRequirements() {
        Map<String, Object> config = securityConfigService.loadSecurityConfig();
        
        List<String> requirements = new ArrayList<>();
        requirements.add("Mínimo " + config.get("minPasswordLength") + " caracteres");
        
        if ((Boolean) config.get("requiresUppercase")) {
            requirements.add("Al menos una letra mayúscula");
        }
        if ((Boolean) config.get("requiresLowercase")) {
            requirements.add("Al menos una letra minúscula");
        }
        if ((Boolean) config.get("requiresNumbers")) {
            requirements.add("Al menos un número");
        }
        if ((Boolean) config.get("requiresSpecial")) {
            requirements.add("Al menos un símbolo (" + config.get("allowedSpecialChars") + ")");
        }
        
        return String.join(", ", requirements);
    }
    
    public int calculatePasswordStrength(String password) {
        if (password == null) return 0;
        
        Map<String, Object> config = securityConfigService.loadSecurityConfig();
        int minLength = (Integer) config.get("minPasswordLength");
        
        int strength = 0;
        
        // Longitud (máximo 40 puntos)
        int lengthScore = Math.min((password.length() * 100) / minLength, 55);
        strength += lengthScore;
        
        // Diversidad de caracteres (máximo 60 puntos)
        if (password.matches(".*[A-Z].*")) strength += 10;
        if (password.matches(".*[a-z].*")) strength += 10;  
        if (password.matches(".*[0-9].*")) strength += 10;
        
        String specialChars = (String) config.get("allowedSpecialChars");
        if (password.matches(".*[" + Pattern.quote(specialChars) + "].*")) strength += 15;
        
        return Math.min(strength, 100);
    }
    
    public Map<String, Object> getPasswordPolicyConfig() {
        return securityConfigService.loadSecurityConfig();
    }
}