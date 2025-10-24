package com.studentgest.user_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.studentgest.user_service.service.SecurityConfigService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Autowired
    private SecurityConfigService securityConfigService;
    
    // Remueve los @Value fijos y usa la configuración de BD
    public String getSecret() {
        return securityConfigService.getStringValue("JWT_SECRET", "mySecretKeyForJWTGenerationInStudentGestApplication2024");
    }
    
    public Long getExpiration() {
        Integer timeoutMinutes = securityConfigService.getIntegerValue("TIMEOUT_SESION_MINUTOS", 15);
        return (long) (timeoutMinutes * 60 * 1000); // Convertir minutos a milisegundos
    }
    
    public Long getInactivityTimeout() {
        Integer timeoutMinutes = securityConfigService.getIntegerValue("TIMEOUT_SESION_MINUTOS", 15);
        return (long) (timeoutMinutes * 60 * 1000);
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(getSecret().getBytes());
    }
    
    public String generateToken(String email, String rol, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol);
        claims.put("userId", userId);
        claims.put("lastActivity", System.currentTimeMillis());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + getExpiration()))
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
            throw new SecurityException("Token expirado", e);
        } catch (JwtException e) {
            throw new SecurityException("Token inválido", e);
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    // Método para verificar inactividad
    public boolean isTokenInactive(String token) {
        try {
            Long lastActivity = extractLastActivity(token);
            Long currentTime = System.currentTimeMillis();
            
            // Verificar si ha pasado más tiempo del permitido sin actividad
            return (currentTime - lastActivity) > getInactivityTimeout();
        } catch (Exception e) {
            return true; // Si hay error, considerar como inactivo
        }
    }
    
    // Método para extraer el timestamp de última actividad
    public Long extractLastActivity(String token) {
        return extractClaim(token, claims -> claims.get("lastActivity", Long.class));
    }
    
    // Método para actualizar la última actividad (renovar token)
    public String refreshToken(String token) {
        try {
            String email = extractUsername(token);
            String rol = extractRol(token);
            Integer userId = extractUserId(token);
            
            // Generar nuevo token con nueva última actividad
            return generateToken(email, rol, userId);
        } catch (Exception e) {
            throw new SecurityException("No se pudo refrescar el token", e);
        }
    }
    
    // Método para validar token considerando inactividad
    public Boolean validateTokenWithInactivity(String token, String email) {
        final String username = extractUsername(token);
        
        // Verificar expiración normal
        boolean isExpired = isTokenExpired(token);
        
        // Verificar inactividad
        boolean isInactive = isTokenInactive(token);
        
        return (username.equals(email) && !isExpired && !isInactive);
    }
    
}