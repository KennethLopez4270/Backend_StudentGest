package com.studentgest.user_service.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PasswordPolicyService {
    
    // Cambiar de 8 a 12 caracteres mínimos
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final boolean REQUIRES_UPPERCASE = true;
    private static final boolean REQUIRES_LOWERCASE = true;
    private static final boolean REQUIRES_NUMBERS = true;
    private static final boolean REQUIRES_SPECIAL = true;
    private static final String SPECIAL_CHARS = "@$!%*?&";
    
    public boolean validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        
        boolean hasUppercase = !REQUIRES_UPPERCASE || password.matches(".*[A-Z].*");
        boolean hasLowercase = !REQUIRES_LOWERCASE || password.matches(".*[a-z].*");
        boolean hasNumbers = !REQUIRES_NUMBERS || password.matches(".*[0-9].*");
        boolean hasSpecial = !REQUIRES_SPECIAL || password.matches(".*[" + Pattern.quote(SPECIAL_CHARS) + "].*");
        
        return hasUppercase && hasLowercase && hasNumbers && hasSpecial;
    }
    
    public String getPasswordRequirements() {
        List<String> requirements = new ArrayList<>();
        requirements.add("Mínimo " + MIN_PASSWORD_LENGTH + " caracteres");
        if (REQUIRES_UPPERCASE) requirements.add("Al menos una letra mayúscula");
        if (REQUIRES_LOWERCASE) requirements.add("Al menos una letra minúscula");
        if (REQUIRES_NUMBERS) requirements.add("Al menos un número");
        if (REQUIRES_SPECIAL) requirements.add("Al menos un símbolo (" + SPECIAL_CHARS + ")");
        
        return String.join(", ", requirements);
    }
    
    // Nuevo método para calcular fortaleza de contraseña
    public int calculatePasswordStrength(String password) {
        if (password == null) return 0;
        
        int strength = 0;
        
        // Longitud (máximo 40 puntos)
        int lengthScore = Math.min(password.length() * 3, 40);
        strength += lengthScore;
        
        // Diversidad de caracteres (máximo 60 puntos)
        if (password.matches(".*[A-Z].*")) strength += 15; // Mayúsculas
        if (password.matches(".*[a-z].*")) strength += 15; // Minúsculas  
        if (password.matches(".*[0-9].*")) strength += 15; // Números
        if (password.matches(".*[" + Pattern.quote(SPECIAL_CHARS) + "].*")) strength += 15; // Símbolos
        
        return Math.min(strength, 100);
    }
}