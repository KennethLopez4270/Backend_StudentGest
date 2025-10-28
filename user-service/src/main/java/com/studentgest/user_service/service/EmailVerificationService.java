package com.studentgest.user_service.service;

import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EmailVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SecurityConfigService securityConfigService; // ✅ CAMBIO
    
    public boolean sendVerificationEmail(User user) {
        try {
            logger.info("Enviando email de verificación para usuario: {}", user.getEmail());
            
            // ✅ CAMBIO: Usar SecurityConfigService
            Map<String, Object> emailConfig = securityConfigService.getEmailConfig();
            int expiryHours = (Integer) emailConfig.get("verificationTokenExpiryHours");
            String baseUrl = (String) emailConfig.get("baseVerificationUrl");
            
            // Generar token único
            String token = UUID.randomUUID().toString();
            
            // Establecer expiración desde configuración
            LocalDateTime expiration = LocalDateTime.now().plusHours(expiryHours);
            
            // Guardar token en el usuario
            user.setTokenVerificacion(token);
            user.setExpiracionTokenVerificacion(Timestamp.valueOf(expiration));
            userRepository.save(user);
            
            // Construir enlace de verificación
            String verificationLink = baseUrl + "/verify-email?token=" + token;
            
            logger.info("Token de verificación generado para {}: {}", user.getEmail(), token);
            
            // Enviar email
            boolean emailSent = emailService.sendVerificationEmail(user.getEmail(), user.getNombre(), verificationLink);
            
            if (emailSent) {
                logger.info("Email de verificación enviado exitosamente a: {}", user.getEmail());
            } else {
                logger.error("Error al enviar email de verificación a: {}", user.getEmail());
            }
            
            return emailSent;
            
        } catch (Exception e) {
            logger.error("Error enviando email de verificación para {}: {}", user.getEmail(), e.getMessage(), e);
            return false;
        }
    }
    
    public boolean verifyEmail(String token) {
        try {
            logger.info("Verificando email con token: {}", token);
            
            Optional<User> userOptional = userRepository.findByTokenVerificacion(token);
            
            if (userOptional.isEmpty()) {
                logger.warn("Token de verificación no encontrado: {}", token);
                return false;
            }
            
            User user = userOptional.get();
            logger.info("Usuario encontrado para verificación: {}", user.getEmail());
            
            // Verificar expiración
            if (user.getExpiracionTokenVerificacion() == null) {
                logger.warn("Token sin fecha de expiración para usuario: {}", user.getEmail());
                return false;
            }
            
            if (user.getExpiracionTokenVerificacion().before(new Timestamp(System.currentTimeMillis()))) {
                logger.warn("Token expirado para usuario: {}", user.getEmail());
                return false;
            }
            
            // Marcar email como verificado
            user.setEstadoGmail("verificado");
            user.setTokenVerificacion(null);
            user.setExpiracionTokenVerificacion(null);
            userRepository.save(user);
            
            logger.info("Email verificado exitosamente para: {}", user.getEmail());
            return true;
            
        } catch (Exception e) {
            logger.error("Error verificando email con token {}: {}", token, e.getMessage(), e);
            return false;
        }
    }
    
    public boolean isUserFullyVerified(Integer userId) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return false;
            }
            
            User user = userOptional.get();
            boolean isVerified = "aprobado".equals(user.getEstado().toString()) && 
                               "verificado".equals(user.getEstadoGmail());
            
            logger.debug("Verificación completa usuario {}: {}", userId, isVerified);
            return isVerified;
            
        } catch (Exception e) {
            logger.error("Error verificando estado completo del usuario {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    // ✅ NUEVO: Reenviar email de verificación
    public boolean resendVerificationEmail(String email) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                logger.warn("Usuario no encontrado para reenviar verificación: {}", email);
                return false;
            }
            
            User user = userOptional.get();
            
            // Si ya está verificado, no hacer nada
            if ("verificado".equals(user.getEstadoGmail())) {
                logger.info("Usuario ya verificado: {}", email);
                return true;
            }
            
            return sendVerificationEmail(user);
            
        } catch (Exception e) {
            logger.error("Error reenviando email de verificación a {}: {}", email, e.getMessage(), e);
            return false;
        }
    }
    
    // ✅ NUEVO: Limpiar tokens expirados
    public void cleanExpiredVerificationTokens() {
        try {
            List<User> usersWithExpiredTokens = userRepository.findUsersWithExpiredVerificationTokens();
            
            for (User user : usersWithExpiredTokens) {
                user.setTokenVerificacion(null);
                user.setExpiracionTokenVerificacion(null);
                userRepository.save(user);
                logger.info("Token expirado limpiado para usuario: {}", user.getEmail());
            }
            
            logger.info("Limpieza de tokens expirados completada. {} tokens limpiados.", usersWithExpiredTokens.size());
            
        } catch (Exception e) {
            logger.error("Error limpiando tokens expirados: {}", e.getMessage(), e);
        }
    }
    
    public boolean isEmailVerificationRequired() {
        // ✅ CAMBIO: Usar SecurityConfigService
        Map<String, Object> emailConfig = securityConfigService.getEmailConfig();
        return (Boolean) emailConfig.get("emailVerificationRequired");
    }
}