package com.studentgest.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CaptchaService {
    
    @Autowired
    private SecurityConfigService securityConfigService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    
    public boolean isCaptchaEnabled() {
        Map<String, Object> captchaConfig = securityConfigService.getCaptchaConfig();
        return (Boolean) captchaConfig.get("captchaEnabled");
    }
    
    public boolean verifyRecaptcha(String recaptchaResponse, String clientIp) {
        try {
            if (recaptchaResponse == null || recaptchaResponse.trim().isEmpty()) {
                return false;
            }
            
            Map<String, Object> captchaConfig = securityConfigService.getCaptchaConfig();
            String secretKey = (String) captchaConfig.get("recaptchaSecretKey");
            
            if (secretKey == null || secretKey.trim().isEmpty()) {
                // Si no hay clave configurada, permitir el paso (para desarrollo)
                return true;
            }
            
            // Verificar con Google reCAPTCHA
            String url = String.format("%s?secret=%s&response=%s&remoteip=%s", 
                RECAPTCHA_VERIFY_URL, secretKey, recaptchaResponse, clientIp);
            
            Map response = restTemplate.postForObject(url, null, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            // En caso de error, registrar pero permitir el paso (para no bloquear usuarios)
            System.err.println("Error verificando reCAPTCHA: " + e.getMessage());
            return true; // Cambiar a false en producción
        }
    }
    
    public String getRecaptchaSiteKey() {
        Map<String, Object> captchaConfig = securityConfigService.getCaptchaConfig();
        return (String) captchaConfig.getOrDefault("recaptchaSiteKey", "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI"); // Clave de prueba
    }
    
    public boolean shouldShowCaptcha(int failedAttempts) {
        Map<String, Object> captchaConfig = securityConfigService.getCaptchaConfig();
        int threshold = (Integer) captchaConfig.get("captchaFailedAttempts");
        return failedAttempts >= threshold;
    }
}