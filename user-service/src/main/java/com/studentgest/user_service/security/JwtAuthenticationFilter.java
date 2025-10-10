package com.studentgest.user_service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    
    // ✅ CORREGIDO: Logger con import correcto
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                email = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.warn("JWT token inválido o expirado: {}", e.getMessage());
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // ✅ NUEVO: Verificar inactividad
                if (jwtUtil.isTokenInactive(jwt)) {
                    logger.warn("Token inactivo por timeout para usuario: {}", email);
                    sendErrorResponse(response, "Sesión inactiva. Por favor, inicie sesión nuevamente.");
                    return;
                }
                
                String rol = jwtUtil.extractRol(jwt);
                Integer userId = jwtUtil.extractUserId(jwt);
                
                if (jwtUtil.validateTokenWithInactivity(jwt, email)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            email, 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol))
                        );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // ✅ NUEVO: Refrescar token en cada request válido
                    String newToken = jwtUtil.refreshToken(jwt);
                    response.setHeader("X-New-Token", newToken);
                    
                    logger.debug("Token refrescado para usuario: {}", email);
                } else {
                    logger.warn("Token inválido para usuario: {}", email);
                    sendErrorResponse(response, "Token de autenticación inválido");
                    return;
                }
            } catch (Exception e) {
                logger.error("Error al validar token JWT", e);
                sendErrorResponse(response, "Error de autenticación");
                return;
            }
        }
        chain.doFilter(request, response);
    }
    
    // ✅ NUEVO: Método para enviar respuestas de error
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"success\": false, \"message\": \"%s\", \"error\": \"SESSION_TIMEOUT\"}", 
            message
        );
        
        response.getWriter().write(jsonResponse);
    }
    
    // ✅ OPCIONAL: Excluir endpoints públicos del filtro
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/users/login") ||
               path.startsWith("/api/users/register") ||
               path.startsWith("/api/users/password-policy") ||
               path.startsWith("/api/users/test-cors");
    }
}