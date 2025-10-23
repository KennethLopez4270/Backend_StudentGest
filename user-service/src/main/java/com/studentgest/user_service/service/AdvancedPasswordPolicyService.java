package com.studentgest.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class AdvancedPasswordPolicyService {
    
    @Autowired
    private SecurityConfigService securityConfigService;
    
    // Lista de contraseñas comunes (deberías cargarlas desde un archivo)
    private final Set<String> commonPasswords = Set.of(
        "password", "123456", "12345678", "123456789", "12345",
        "qwerty", "abc123", "password1", "1234567", "1234567890",
        "admin", "welcome", "monkey", "password123", "123123"
    );
    
    public PasswordStrengthResult evaluatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrengthResult(0, "Débil", Arrays.asList("La contraseña es requerida"));
        }
    
        // ✅ CAMBIO: Usar SecurityConfigService en lugar de AppConfigService
        Map<String, Object> config = securityConfigService.getPasswordConfig();
        int minLength = (Integer) config.get("minLength");
        boolean requiresUppercase = (Boolean) config.get("requiresUppercase");
        boolean requiresLowercase = (Boolean) config.get("requiresLowercase");
        boolean requiresNumbers = (Boolean) config.get("requiresNumbers");
        boolean requiresSpecial = (Boolean) config.get("requiresSpecial");
        String specialChars = (String) config.get("allowedSpecialChars");
    
        int score = 0;
        List<String> feedback = new ArrayList<>();
    
        // Longitud
        if (password.length() >= minLength) score += 40;
        else feedback.add("Mínimo " + minLength + " caracteres");
    
        // Mayúsculas (no contar como especiales)
        if (requiresUppercase && password.matches(".*[A-Z].*")) score += 15;
        else if (requiresUppercase) feedback.add("Falta mayúscula");
    
        // Minúsculas
        if (requiresLowercase && password.matches(".*[a-z].*")) score += 15;
        else if (requiresLowercase) feedback.add("Falta minúscula");
    
        // Números
        if (requiresNumbers && password.matches(".*\\d.*")) score += 15;
        else if (requiresNumbers) feedback.add("Falta número");
    
        // Especiales (separado de mayúsculas)
        if (requiresSpecial && password.matches(".*[" + Pattern.quote(specialChars) + "].*")) score += 15;
        else if (requiresSpecial) feedback.add("Falta símbolo especial (" + specialChars + ")");
    
        // Bonus por diversidad extra
        if (password.length() > minLength + 4) score += 10;
    
        // Penalizaciones por patrones débiles
        if (hasSequentialChars(password)) {
            score = Math.max(0, score - 10);
            feedback.add("Patrón secuencial detectado");
        }
        
        if (hasRepeatedChars(password)) {
            score = Math.max(0, score - 10);
            feedback.add("Demasiados caracteres repetidos");
        }
        
        if (commonPasswords.contains(password.toLowerCase())) {
            score = Math.max(0, score - 20);
            feedback.add("Contraseña demasiado común");
        }
    
        score = Math.min(100, score);
        String strength = getStrengthLevel(score);
    
        return new PasswordStrengthResult(score, strength, feedback);
    }
    
    private int calculateLengthScore(String password, int minLength) {
        int length = password.length();
        if (length < minLength) return 0;
        if (length < 12) return 10;
        if (length < 16) return 20;
        return 30;
    }
    
    private int calculatePatternScore(String password) {
        int patternScore = 0;
        
        // Bonus por mezcla de tipos de caracteres
        int charTypeCount = 0;
        if (password.matches(".*[a-z].*")) charTypeCount++;
        if (password.matches(".*[A-Z].*")) charTypeCount++;
        if (password.matches(".*[0-9].*")) charTypeCount++;
        if (password.matches(".*[^a-zA-Z0-9].*")) charTypeCount++;
        
        patternScore += (charTypeCount - 1) * 5; // 5-15 puntos
        
        // Penalización por información personal (ejemplo básico)
        if (password.matches(".*(123|abc|qwerty|admin).*")) {
            patternScore -= 10;
        }
        
        return Math.max(0, patternScore);
    }
    
    private boolean hasSequentialChars(String password) {
        String lowerPassword = password.toLowerCase();
        String[] sequences = {"123", "234", "345", "456", "567", "678", "789",
                             "abc", "bcd", "cde", "def", "efg", "fgh", "ghi",
                             "qwerty", "asdfgh", "zxcvbn"};
        
        for (String seq : sequences) {
            if (lowerPassword.contains(seq)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasRepeatedChars(String password) {
        // Verificar si un carácter se repite 3+ veces consecutivas
        return password.matches(".*(.)\\1\\1.*");
    }
    
    private String getStrengthLevel(int score) {
        if (score < 40) return "Muy débil";
        if (score < 60) return "Débil";
        if (score < 80) return "Moderada";
        if (score < 90) return "Fuerte";
        return "Muy fuerte";
    }
    
    public static class PasswordStrengthResult {
        private int score;
        private String strength;
        private List<String> feedback;
        
        public PasswordStrengthResult(int score, String strength, List<String> feedback) {
            this.score = score;
            this.strength = strength;
            this.feedback = feedback;
        }
        
        // getters
        public int getScore() { return score; }
        public String getStrength() { return strength; }
        public List<String> getFeedback() { return feedback; }
    }
}