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
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.info("🎯 INICIANDO FILTRO para: {} {}", method, requestURI);

        // ✅ SI ES PÚBLICO O ES OPTIONS (CORS), PASAR DIRECTAMENTE
        if ("OPTIONS".equalsIgnoreCase(method) || isPublicEndpoint(requestURI, method)) {
            logger.info("✅ Endpoint público o OPTIONS, pasando filtro: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        logger.info("🔐 Endpoint protegido, verificando autenticación: {}", requestURI);

        final String authorizationHeader = request.getHeader("Authorization");
        logger.info("📨 Authorization Header: {}", authorizationHeader);

        String email = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.info("✅ Token JWT encontrado, longitud: {}", jwt.length());

            try {
                email = jwtUtil.extractUsername(jwt);
                logger.info("👤 Usuario extraído del token: {}", email);

            } catch (Exception e) {
                logger.error("❌ Error al extraer username del token: {}", e.getMessage());
                sendErrorResponse(response, "Token inválido: " + e.getMessage());
                return;
            }
        } else {
            logger.warn("❌ No hay Authorization header o formato incorrecto");
            sendErrorResponse(response, "Token de autorización requerido");
            return;
        }

        if (email != null) {
            logger.info("🔐 Verificando autenticación para usuario: {}", email);

            try {
                boolean isValid = jwtUtil.validateTokenWithInactivity(jwt, email);
                logger.info("✅ Resultado validación token: {}", isValid);

                if (isValid) {
                    String rol = jwtUtil.extractRol(jwt);
                    Integer userId = jwtUtil.extractUserId(jwt);

                    logger.info("🎉 Autenticación exitosa - Usuario: {}, Rol: {}", email, rol);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol)));
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Refrescar token
                    String newToken = jwtUtil.refreshToken(jwt);
                    response.setHeader("X-New-Token", newToken);

                    logger.info("🔄 Token refrescado para usuario: {}", email);

                } else {
                    logger.warn("🚫 Token inválido para usuario: {}", email);
                    sendErrorResponse(response, "Token de autenticación inválido o expirado");
                    return;
                }

            } catch (Exception e) {
                logger.error("💥 Error durante la validación del token: {}", e.getMessage());
                sendErrorResponse(response, "Error de autenticación: " + e.getMessage());
                return;
            }
        }

        logger.info("➡️ Continuando cadena de filtros para: {}", requestURI);
        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestURI, String method) {
        boolean isPublic =
                // Endpoints de usuarios públicos
                requestURI.startsWith("/api/users") || // ✅ LIBERADO COMPLETAMENTE SEGÚN SOLICITUD
                        (requestURI.equals("/api/users") && "POST".equalsIgnoreCase(method)) ||
                        requestURI.equals("/api/users/login") ||
                        requestURI.startsWith("/api/users/reset-password") ||
                        requestURI.startsWith("/api/users/password-policy") ||
                        requestURI.startsWith("/api/users/public/") ||
                        requestURI.equals("/api/users/test-cors") ||
                        requestURI.equals("/api/users/debug-login") ||

                        // Endpoints de seguridad públicos
                        requestURI.startsWith("/api/security-config/password-policy") ||
                        requestURI.startsWith("/api/security-config/category/") ||

                        // ✅ ENDPOINTS DE RECUPERACIÓN DE CONTRASEÑA
                        requestURI.startsWith("/api/password-recovery/") ||

                        // ✅ CAPTCHA endpoints
                        requestURI.startsWith("/api/captcha/") ||

                        // ✅ Password strength endpoints
                        requestURI.startsWith("/api/password-strength/") ||

                        // ✅ Email validation endpoints
                        requestURI.startsWith("/api/email/validate") ||
                        requestURI.startsWith("/api/email-verification/") ||

                        // ✅ App config endpoints
                        requestURI.startsWith("/api/app-config/") ||

                        // ✅ Assets endpoints
                        requestURI.startsWith("/api/assets") ||

                        // ✅ Roles endpoint (SOLO GET, recursivo)
                        ((requestURI.equals("/api/roles") || requestURI.startsWith("/api/roles/"))
                                && "GET".equalsIgnoreCase(method))
                        ||

                        // Endpoints de debug temporalmente como públicos
                        requestURI.equals("/api/users/debug-token-simple") ||
                        requestURI.equals("/api/users/debug-token") ||
                        requestURI.equals("/api/users/verify-session");

        logger.debug("🔍 Verificando endpoint: {} {} -> {}", method, requestURI, isPublic ? "PUBLICO" : "PROTEGIDO");
        return isPublic;
    }

    // Método para enviar respuestas de error
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        logger.warn("🚨 Enviando error de autenticación: {}", message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"%s\", \"error\": \"AUTH_ERROR\"}",
                message);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean isPublic = isPublicEndpoint(path, method);

        logger.info("🔍 shouldNotFilter - {} {} -> {}", method, path, isPublic ? "PUBLICO" : "PROTEGIDO");
        if (path.contains("password-recovery")) {
            logger.info("🔐 PASSWORD RECOVERY ENDPOINT - {} {} -> {}", method, path,
                    isPublic ? "PUBLICO" : "PROTEGIDO");
        }
        return isPublic;
    }
}