package com.studentgest.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class AdvancedPasswordPolicyService {
    
    @Autowired
    private SecurityConfigService securityConfigService;
    
    // Lista de contraseñas comunes (ampliada)
    private final Set<String> commonPasswords = Set.of(
        "password", "123456", "12345678", "123456789", "12345", "1234567890",
        "qwerty", "abc123", "password1", "1234567", "123123", "111111",
        "admin", "welcome", "monkey", "password123", "000000", "1234",
        "123456789012", "12345678901", "0123456789"
    );
    
    public PasswordStrengthResult evaluatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrengthResult(0, "Muy débil", Arrays.asList("La contraseña es requerida"));
        }
    
        Map<String, Object> config = securityConfigService.getPasswordConfig();
        int minLength = (Integer) config.get("minLength");
        boolean requiresUppercase = (Boolean) config.get("requiresUppercase");
        boolean requiresLowercase = (Boolean) config.get("requiresLowercase");
        boolean requiresNumbers = (Boolean) config.get("requiresNumbers");
        boolean requiresSpecial = (Boolean) config.get("requiresSpecial");
        String specialChars = (String) config.get("allowedSpecialChars");

        int score = 0;
        List<String> feedback = new ArrayList<>();

        System.out.println("Evaluando contraseña: " + password);
        System.out.println("Configuración: minLength=" + minLength + 
                         ", requiresUppercase=" + requiresUppercase +
                         ", requiresLowercase=" + requiresLowercase +
                         ", requiresNumbers=" + requiresNumbers +
                         ", requiresSpecial=" + requiresSpecial +
                         ", specialChars=" + specialChars);

        // 1. Longitud (40 puntos máximo)
        if (password.length() >= minLength) {
            score += 40;
            System.out.println("Longitud adecuada: +40 puntos");
        } else {
            feedback.add("Mínimo " + minLength + " caracteres");
            System.out.println("Longitud insuficiente");
        }

        // 2. Mayúsculas (15 puntos si está requerido)
        boolean hasUppercase = password.matches(".*[A-Z].*");
        if (requiresUppercase) {
            if (hasUppercase) {
                score += 15;
                System.out.println("Tiene mayúscula: +15 puntos");
            } else {
                feedback.add("Falta mayúscula");
                System.out.println("No tiene mayúscula");
            }
        }

        // 3. Minúsculas (15 puntos si está requerido)
        boolean hasLowercase = password.matches(".*[a-z].*");
        if (requiresLowercase) {
            if (hasLowercase) {
                score += 15;
                System.out.println("Tiene minúscula: +15 puntos");
            } else {
                feedback.add("Falta minúscula");
                System.out.println("No tiene minúscula");
            }
        }

        // 4. Números (15 puntos si está requerido)
        boolean hasNumbers = password.matches(".*\\d.*");
        if (requiresNumbers) {
            if (hasNumbers) {
                score += 15;
                System.out.println("Tiene números: +15 puntos");
            } else {
                feedback.add("Falta número");
                System.out.println("No tiene números");
            }
        }

        // 5. Caracteres especiales (15 puntos si está requerido)
        boolean hasSpecial = false;
        if (requiresSpecial) {
            String escapedSpecialChars = Pattern.quote(specialChars);
            hasSpecial = password.matches(".*[" + escapedSpecialChars + "].*");
            
            if (hasSpecial) {
                score += 15;
                System.out.println("Tiene caracteres especiales: +15 puntos");
            } else {
                feedback.add("Falta símbolo especial (" + specialChars + ")");
                System.out.println(" No tiene caracteres especiales");
            }
            
            if (hasSpecial) {
                List<Character> foundSpecials = new ArrayList<>();
                for (char c : password.toCharArray()) {
                    if (specialChars.indexOf(c) >= 0) {
                        foundSpecials.add(c);
                    }
                }
                System.out.println("Caracteres especiales encontrados: " + foundSpecials);
            }
        }

        // 6. ANÁLISIS DE DIVERSIDAD DE CARACTERES (NUEVO)
        int characterTypes = countCharacterTypes(password, requiresUppercase, requiresLowercase, requiresNumbers, requiresSpecial, specialChars);
        System.out.println("Tipos de caracteres diferentes: " + characterTypes);
        
        // Penalización severa por falta de diversidad
        if (characterTypes < 2) {
            score = Math.max(0, score - 30);
            feedback.add("Muy poca diversidad de caracteres");
            System.out.println("Penalización severa por falta de diversidad: -30 puntos");
        } else if (characterTypes < 3) {
            score = Math.max(0, score - 15);
            feedback.add("Poca diversidad de caracteres");
            System.out.println(" Penalización por poca diversidad: -15 puntos");
        }

        // 7. Bonus por diversidad extra (solo si tiene buena diversidad)
        if (characterTypes >= 3 && password.length() > minLength + 4) {
            score += 10;
            System.out.println("Bonus por diversidad y longitud: +10 puntos");
        }

        // 8. PENALIZACIONES MÁS ESTRICTAS POR PATRONES DÉBILES
        if (hasSequentialChars(password)) {
            score = Math.max(0, score - 20);
            feedback.add("Patrón secuencial detectado");
            System.out.println("Penalización por secuencia: -20 puntos");
        }
        
        if (hasRepeatedChars(password)) {
            score = Math.max(0, score - 15);
            feedback.add("Demasiados caracteres repetidos");
            System.out.println(" Penalización por repetición: -15 puntos");
        }
        
        if (isOnlyNumbers(password)) {
            score = Math.max(0, score - 25);
            feedback.add("Solo contiene números");
            System.out.println(" Penalización por solo números: -25 puntos");
        }
        
        if (isOnlyLetters(password)) {
            score = Math.max(0, score - 20);
            feedback.add("Solo contiene letras");
            System.out.println(" Penalización por solo letras: -20 puntos");
        }
        
        if (commonPasswords.contains(password.toLowerCase())) {
            score = Math.max(0, score - 30);
            feedback.add("Contraseña demasiado común");
            System.out.println(" Penalización por contraseña común: -30 puntos");
        }

        // Asegurar que el score no sea negativo ni mayor a 100
        score = Math.max(0, Math.min(100, score));
        
        // NUEVA LÓGICA: Si tiene menos de 3 tipos de caracteres, limitar fuerza máxima
        if (characterTypes < 3 && score > 50) {
            score = 50;
            System.out.println("Limitación por baja diversidad: score máximo 50");
        }
        
        String strength = getStrengthLevel(score);

        System.out.println("Puntuación final: " + score + " - " + strength);
        System.out.println("Feedback: " + feedback);
        System.out.println("═══════════════════════════════════════════");

        return new PasswordStrengthResult(score, strength, feedback);
    }
    
    // NUEVO MÉTODO: Contar tipos de caracteres diferentes
    private int countCharacterTypes(String password, boolean requiresUppercase, boolean requiresLowercase, 
                                  boolean requiresNumbers, boolean requiresSpecial, String specialChars) {
        int types = 0;
        
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecialChar = requiresSpecial && password.matches(".*[" + Pattern.quote(specialChars) + "].*");
        
        if (hasUpper) types++;
        if (hasLower) types++;
        if (hasNumber) types++;
        if (hasSpecialChar) types++;
        
        return types;
    }
    
    // NUEVO MÉTODO: Verificar si solo tiene números
    private boolean isOnlyNumbers(String password) {
        return password.matches("^[0-9]+$");
    }
    
    // NUEVO MÉTODO: Verificar si solo tiene letras
    private boolean isOnlyLetters(String password) {
        return password.matches("^[a-zA-Z]+$");
    }
    
    private boolean hasSequentialChars(String password) {
        String lowerPassword = password.toLowerCase();
        
        // Secuencias numéricas
        String[] numericSequences = {
            "123", "234", "345", "456", "567", "678", "789", "890",
            "012", "1234", "2345", "3456", "4567", "5678", "6789", "7890",
            "12345", "23456", "34567", "45678", "56789", "67890"
        };
        
        // Secuencias de teclado
        String[] keyboardSequences = {
            "qwerty", "asdfgh", "zxcvbn", "qwert", "asdfg", "zxcvb",
            "qaz", "wsx", "edc", "rfv", "tgb", "yhn", "ujm", "ik", "ol",
            "qwe", "asd", "zxc"
        };
        
        for (String seq : numericSequences) {
            if (lowerPassword.contains(seq)) {
                return true;
            }
        }
        
        for (String seq : keyboardSequences) {
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
    
    // NUEVA LÓGICA MÁS ESTRICTA PARA CLASIFICACIÓN
    private String getStrengthLevel(int score) {
        if (score < 30) return "Muy débil";
        if (score < 50) return "Débil";
        if (score < 70) return "Moderada";
        if (score < 85) return "Fuerte";
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