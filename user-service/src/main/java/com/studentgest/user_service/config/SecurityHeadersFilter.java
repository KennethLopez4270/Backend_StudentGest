package com.studentgest.user_service.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro de seguridad para agregar headers HTTP de protección.
 * Mitiga vulnerabilidades detectadas por OWASP ZAP:
 * - A05:2021 Security Misconfiguration
 * - A03:2021 Injection (XSS via CSP)
 */
@Component
@Order(1)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Content Security Policy - Previene XSS
        // Permite scripts solo de 'self' y dominios confiados de Google (para
        // reCAPTCHA)
        String csp = "default-src 'self'; " +
                "script-src 'self' https://www.google.com https://www.gstatic.com; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com; " +
                "img-src 'self' data: https:; " +
                "connect-src 'self' http://localhost:* https://*.netlify.app https://*.herokuapp.com https://www.google.com; "
                +
                "frame-src https://www.google.com; " +
                "upgrade-insecure-requests;";
        httpResponse.setHeader("Content-Security-Policy", csp);

        // Previene MIME type sniffing
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // Previene clickjacking
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // Habilita protección XSS del navegador (legacy pero útil)
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // Fuerza HTTPS en producción (HSTS)
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Controla información del Referer
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Deshabilita APIs peligrosas del navegador
        httpResponse.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=(), usb=()");

        // Cache control para datos sensibles
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        httpResponse.setHeader("Pragma", "no-cache");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
