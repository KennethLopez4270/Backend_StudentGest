package com.studentgest.user_service.config;

import com.studentgest.user_service.security.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("🔐 INICIANDO CONFIGURACIÓN DE SEGURIDAD...");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // ✅ PERMITIR OPTIONS (CORS) GLOBALMENTE
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ ENDPOINTS PÚBLICOS DE USUARIOS
                        .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // Registro
                        .requestMatchers("/api/users/public/**", "/api/users/debug/**").permitAll()
                        .requestMatchers("/api/users/reset-password/**").permitAll()
                        .requestMatchers("/api/users/password-policy", "/api/users/simple-password-policy").permitAll()
                        .requestMatchers("/api/users/test-cors", "/api/users/verify-session").permitAll()

                        // ✅ ABM USUARIOS (LIBERADO SEGÚN SOLICITUD)
                        .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").permitAll()
                        // ✅ Endpoints específicos de gestión de estado (Explícitos)
                        .requestMatchers("/api/users/activar/**", "/api/users/desactivar/**").permitAll()
                        .requestMatchers("/api/users/aprobar/**", "/api/users/desaprobar/**").permitAll()
                        .requestMatchers("/api/users/desbloquear/**").permitAll()

                        // ✅ OTROS ENDPOINTS PÚBLICOS
                        .requestMatchers("/api/security-config/**").permitAll()
                        .requestMatchers("/api/captcha/**", "/api/email-verification/**", "/api/email/**").permitAll()
                        .requestMatchers("/api/password-strength/**", "/api/app-config/**").permitAll()
                        .requestMatchers("/api/assets/**").permitAll()
                        .requestMatchers("/api/password-change/**", "/api/password-recovery/**").permitAll()
                        .requestMatchers("/error").permitAll() // ✅ PERMITIR ERROR CONTROLLER

                        // ✅ ROLES (TOTALMENTE PÚBLICO - SOLICITADO POR USUARIO)
                        .requestMatchers("/api/roles/**").permitAll()

                        // ❌ RESTO REQUIERE AUTENTICACIÓN
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("🔐 CONFIGURACIÓN DE SEGURIDAD COMPLETADA ✅ TODOS LOS ENDPOINTS PÚBLICOS HABILITADOS");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // ✅ Incluye localhost:5173 (dev), localhost:4173 (preview) y producción
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:4173",
                "http://localhost:5174",
                "https://proyecto-seguridad-studengest.netlify.app",
                "https://frt-studentgest.netlify.app"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}