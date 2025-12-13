package com.studentgest.user_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.studentgest.user_service.service.SecurityConfigService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Autowired
    private SecurityConfigService securityConfigService;
    
    // Remueve los @Value fijos y usa la configuración de BD
    public String getSecret() {
        return securityConfigService.getStringValue("JWT_SECRET", "mySecretKeyForJWTGenerationInStudentGestApplication2024");
    }
    
    public Long getExpiration() {
        try {
            // PRIMERO intentar con JWT_EXPIRATION_HOURS (24 horas de tu BD)
            Integer expirationHours = securityConfigService.getIntegerValue("JWT_EXPIRATION_HOURS", null);
            if (expirationHours != null) {
                Long expirationMs = (long) (expirationHours * 60 * 60 * 1000);
                logger.info("🔐 JWT Expiration from JWT_EXPIRATION_HOURS: {} hours ({} ms)", expirationHours, expirationMs);
                return expirationMs;
            }
            
            // Fallback a TIMEOUT_SESION_MINUTOS
            Integer timeoutMinutes = securityConfigService.getIntegerValue("TIMEOUT_SESION_MINUTOS", 15);
            Long expirationMs = (long) (timeoutMinutes * 60 * 1000);
            logger.info("JWT Expiration from TIMEOUT_SESION_MINUTOS: {} minutes ({} ms)", timeoutMinutes, expirationMs);
            return expirationMs;
            
        } catch (Exception e) {
            logger.warn("Error al cargar expiration, usando valor por defecto: 24 horas");
            return 24 * 60 * 60 * 1000L; // 24 horas por defecto
        }
    }
    
    
    public Long getInactivityTimeout() {
        try {
            Integer timeoutMinutes = securityConfigService.getIntegerValue("TIMEOUT_SESION_MINUTOS", 15);
            Long inactivityMs = (long) (timeoutMinutes * 60 * 1000);
            logger.debug("JWT Inactivity timeout: {} minutes ({} ms)", timeoutMinutes, inactivityMs);
            return inactivityMs;
        } catch (Exception e) {
            logger.warn("Error al cargar inactivity timeout, usando valor por defecto");
            return 15 * 60 * 1000L;
        }
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(getSecret().getBytes());
    }
    public Map<String, Object> debugToken(String token) {
        Map<String, Object> debugInfo = new HashMap<>();
        try {
            String username = extractUsername(token);
            Date expiration = extractExpiration(token);
            Long lastActivity = extractLastActivity(token);
            Long currentTime = System.currentTimeMillis();
            
            debugInfo.put("username", username);
            debugInfo.put("expiration", expiration.toString());
            debugInfo.put("lastActivity", lastActivity);
            debugInfo.put("currentTime", currentTime);
            debugInfo.put("isExpired", isTokenExpired(token));
            debugInfo.put("isInactive", isTokenInactive(token));
            debugInfo.put("timeUntilExpiration", expiration.getTime() - currentTime);
            debugInfo.put("timeSinceLastActivity", currentTime - lastActivity);
            debugInfo.put("timeoutConfig", getExpiration());
            debugInfo.put("inactivityTimeout", getInactivityTimeout());
            debugInfo.put("success", true);
            
            logger.info("🔍 Debug Token - Usuario: {}, Expira: {}, Válido: {}", 
                       username, expiration, !isTokenExpired(token));
                       
        } catch (Exception e) {
            debugInfo.put("success", false);
            debugInfo.put("error", e.getMessage());
            logger.error("Error en debugToken: {}", e.getMessage());
        }
        return debugInfo;
    }
    public String generateToken(String email, String rol, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol);
        claims.put("userId", userId);
        claims.put("lastActivity", System.currentTimeMillis());
        
        long expirationMs = getExpiration();
        
        // CORREGIR: Usar la misma zona horaria para issuedAt y expiration
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(System.currentTimeMillis() + expirationMs);
        
        // LOG PARA DEBUG
        logger.info("GENERANDO NUEVO TOKEN JWT:");
        logger.info("Email: {}", email);
        logger.info("Rol: {}", rol);
        logger.info("UserId: {}", userId);
        logger.info("Expiración configurada (ms): {}", expirationMs);
        logger.info("Creado el: {}", issuedAt);
        logger.info("Expira el: {}", expirationDate);
        logger.info("Diferencia: {} minutos", expirationMs / (60 * 1000));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + getExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String email) {
        final String username = extractUsername(token);
        return (username.equals(email) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expirado: {}", e.getMessage());
            throw new SecurityException("Token expirado", e);
        } catch (JwtException e) {
            logger.warn("Token JWT inválido: {}", e.getMessage());
            throw new SecurityException("Token inválido", e);
        }
    }

    // CAMBIADO DE private A public
    public Boolean isTokenExpired(String token) {
        try {
            boolean expired = extractExpiration(token).before(new Date());
            if (expired) {
                logger.debug("Token expirado detectado");
            }
            return expired;
        } catch (Exception e) {
            logger.warn("Error al verificar expiración del token: {}", e.getMessage());
            return true;
        }
    }
    
    // Método para verificar inactividad
    public boolean isTokenInactive(String token) {
        try {
            Long lastActivity = extractLastActivity(token);
            Long currentTime = System.currentTimeMillis();
            Long timeSinceLastActivity = currentTime - lastActivity;
            Long inactivityTimeout = getInactivityTimeout();
            
            boolean inactive = timeSinceLastActivity > inactivityTimeout;
            
            if (inactive) {
                logger.warn("Token inactivo: {} ms desde última actividad (timeout: {} ms)", 
                           timeSinceLastActivity, inactivityTimeout);
            } else {
                logger.debug("Token activo: {} ms desde última actividad", timeSinceLastActivity);
            }
            
            return inactive;
        } catch (Exception e) {
            logger.warn("Error al verificar inactividad del token: {}", e.getMessage());
            return true; // Si hay error, considerar como inactivo
        }
    }
    
    // Método para extraer el timestamp de última actividad
    public Long extractLastActivity(String token) {
        try {
            return extractClaim(token, claims -> claims.get("lastActivity", Long.class));
        } catch (Exception e) {
            logger.warn("Error al extraer última actividad: {}", e.getMessage());
            return 0L;
        }
    }
    
    // Método para actualizar la última actividad (renovar token)
    public String refreshToken(String token) {
        try {
            String email = extractUsername(token);
            String rol = extractRol(token);
            Integer userId = extractUserId(token);
            
            logger.debug("Refrescando token para usuario: {}", email);
            
            // Generar nuevo token con nueva última actividad
            return generateToken(email, rol, userId);
        } catch (Exception e) {
            logger.error("Error al refrescar token: {}", e.getMessage());
            throw new SecurityException("No se pudo refrescar el token", e);
        }
    }
    
    // Método para validar token considerando inactividad
    public Boolean validateTokenWithInactivity(String token, String email) {
        try {
            final String username = extractUsername(token);
            
            // Verificar expiración normal
            boolean isExpired = isTokenExpired(token);
            
            // Verificar inactividad
            boolean isInactive = isTokenInactive(token);
            
            boolean isValid = (username.equals(email) && !isExpired && !isInactive);
            
            logger.debug("Validación token - Usuario: {}, Expired: {}, Inactive: {}, Valid: {}", 
                        username, isExpired, isInactive, isValid);
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error en validación completa del token: {}", e.getMessage());
            return false;
        }
    }
    
    
}