package com.studentgest.user_service.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro para agregar headers de seguridad a todas las respuestas HTTP.
 * Mitiga vulnerabilidades detectadas por OWASP ZAP:
 * - XSS (A05:2025 - Injection)
 * - Security Misconfiguration (A02:2025)
 * 
 * CSP: Sin 'unsafe-inline' para scripts (máxima protección XSS)
 * Con 'unsafe-inline' para styles (requerido por Vue.js)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                        throws IOException, ServletException {

                HttpServletResponse httpResponse = (HttpServletResponse) response;

                // ✅ Content Security Policy (CSP) - Previene XSS
                // Scripts SIN unsafe-inline (máxima protección)
                // Styles CON unsafe-inline (requerido por Vue.js)
                httpResponse.setHeader("Content-Security-Policy",
                                "default-src 'self'; " +
                                                "script-src 'self' https://www.google.com https://www.gstatic.com; " +
                                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                                                "font-src 'self' https://fonts.gstatic.com data:; " +
                                                "img-src 'self' data: https: blob:; " +
                                                "frame-src https://www.google.com; " +
                                                "connect-src 'self' http://localhost:* https://www.google.com https://*.netlify.app https://*.herokuapp.com; "
                                                +
                                                "object-src 'none'; " +
                                                "base-uri 'self'; " +
                                                "form-action 'self';");

                // ✅ X-Content-Type-Options - Previene MIME type sniffing
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");

                // ✅ X-Frame-Options - Previene clickjacking
                httpResponse.setHeader("X-Frame-Options", "DENY");

                // ✅ X-XSS-Protection - Protección XSS adicional (navegadores legacy)
                httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

                // ✅ Strict-Transport-Security (HSTS) - Fuerza HTTPS
                httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

                // ✅ Referrer-Policy - Controla qué información se envía en Referer
                httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                // ✅ Permissions-Policy - Restringe features del navegador
                httpResponse.setHeader("Permissions-Policy",
                                "geolocation=(), microphone=(), camera=(), payment=()");

                chain.doFilter(request, response);
        }
}
