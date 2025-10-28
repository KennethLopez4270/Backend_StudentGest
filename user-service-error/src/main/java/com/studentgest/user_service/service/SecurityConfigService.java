package com.studentgest.user_service.service;

import com.studentgest.user_service.model.SecurityPolicy;
import com.studentgest.user_service.repository.SecurityPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SecurityConfigService {
    
    @Autowired
    private SecurityPolicyRepository securityPolicyRepository;
    
    // Cache para mejor performance
    private final Map<String, Object> configCache = new HashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 60000; // 1 minuto
    
    /**
     * Obtener valor de política como String
     */
    public String getStringValue(String policyName, String defaultValue) {
        refreshCacheIfNeeded();
        
        if (configCache.containsKey(policyName)) {
            return (String) configCache.get(policyName);
        }
        
        Optional<SecurityPolicy> policy = securityPolicyRepository.findByNombrePolitica(policyName);
        String value = policy.map(SecurityPolicy::getValor).orElse(defaultValue);
        configCache.put(policyName, value);
        return value;
    }
    
    /**
     * Obtener valor de política como Integer
     */
    public Integer getIntegerValue(String policyName, Integer defaultValue) {
        refreshCacheIfNeeded();
        
        if (configCache.containsKey(policyName)) {
            return (Integer) configCache.get(policyName);
        }
        
        Optional<SecurityPolicy> policy = securityPolicyRepository.findByNombrePolitica(policyName);
        Integer value = policy.map(SecurityPolicy::getValorComoInteger).orElse(defaultValue);
        configCache.put(policyName, value);
        return value;
    }
    
    /**
     * Obtener valor de política como Boolean
     */
    public Boolean getBooleanValue(String policyName, Boolean defaultValue) {
        refreshCacheIfNeeded();
        
        if (configCache.containsKey(policyName)) {
            return (Boolean) configCache.get(policyName);
        }
        
        Optional<SecurityPolicy> policy = securityPolicyRepository.findByNombrePolitica(policyName);
        Boolean value = policy.map(SecurityPolicy::getValorComoBoolean).orElse(defaultValue);
        configCache.put(policyName, value);
        return value;
    }
    
    /**
     * Obtener todas las políticas de una categoría
     */
    public List<SecurityPolicy> getPoliciesByCategory(String category) {
        return securityPolicyRepository.findByCategoria(category);
    }
    
    /**
     * Actualizar una política
     */
    public boolean updatePolicy(String policyName, String newValue) {
        Optional<SecurityPolicy> policyOpt = securityPolicyRepository.findByNombrePolitica(policyName);
        
        if (policyOpt.isPresent()) {
            SecurityPolicy policy = policyOpt.get();
            if (policy.getEditable()) {
                policy.setValor(newValue);
                securityPolicyRepository.save(policy);
                clearCache(); // Limpiar cache después de actualizar
                return true;
            }
        }
        return false;
    }
    
    /**
     * Cargar todas las configuraciones de seguridad
     */
    public Map<String, Object> loadSecurityConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // Políticas de contraseñas
        config.put("minPasswordLength", getIntegerValue("LONGITUD_MINIMA_CONTRASENA", 12));
        config.put("requiresUppercase", getBooleanValue("REQUIERE_MAYUSCULAS", true));
        config.put("requiresLowercase", getBooleanValue("REQUIERE_MINUSCULAS", true));
        config.put("requiresNumbers", getBooleanValue("REQUIERE_NUMEROS", true));
        config.put("requiresSpecial", getBooleanValue("REQUIERE_SIMBOLOS", true));
        config.put("allowedSpecialChars", getStringValue("SIMBOLOS_PERMITIDOS", "@$!%*?&"));
        config.put("passwordHistorySize", getIntegerValue("HISTORICO_CONTRASENAS", 5));
        config.put("passwordExpiryDays", getIntegerValue("DIAS_EXPIRACION_CONTRASENA", 90));
        config.put("minPasswordStrength", getIntegerValue("FUERZA_MINIMA_CONTRASENA", 75));
        
        // Políticas de sesión
        config.put("sessionTimeoutMinutes", getIntegerValue("TIMEOUT_SESION_MINUTOS", 15));
        config.put("sessionWarningMinutes", getIntegerValue("TIMEOUT_ADVERTENCIA_MINUTOS", 5));
        config.put("maxLoginAttempts", getIntegerValue("MAXIMO_INTENTOS_LOGIN", 3));
        config.put("lockoutTimeMinutes", getIntegerValue("TIEMPO_BLOQUEO_MINUTOS", 30));
        
        //
        Integer minLength = getIntegerValue("LONGITUD_MINIMA_CONTRASENA", 12);
    System.out.println("🔐 Cargando LONGITUD_MINIMA_CONTRASENA desde BD: " + minLength);
    
    config.put("minPasswordLength", minLength);
    config.put("requiresUppercase", getBooleanValue("REQUIERE_MAYUSCULAS", true));
    config.put("requiresLowercase", getBooleanValue("REQUIERE_MINUSCULAS", true));
    config.put("requiresNumbers", getBooleanValue("REQUIERE_NUMEROS", true));
    config.put("requiresSpecial", getBooleanValue("REQUIERE_SIMBOLOS", true));
    config.put("allowedSpecialChars", getStringValue("SIMBOLOS_PERMITIDOS", "@$!%*?&"));
    
    System.out.println("🔐 Configuración final cargada: " + config);
        return config;
    }
    
    private void refreshCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_DURATION) {
            clearCache();
            lastCacheUpdate = currentTime;
        }
    }
    
    private void clearCache() {
        configCache.clear();
    }
    
}