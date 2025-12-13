// Crear una nueva clase: PasswordExpirationChecker.java
package com.studentgest.user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PasswordExpirationChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordExpirationChecker.class);
    
    @Autowired
    private UserRepository userRepository;
    
    // Ejecutar todos los días a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkPasswordExpirations() {
        try {
            logger.info("🔍 Iniciando verificación de expiración de contraseñas...");
            
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            List<User> usuarios = userRepository.findAll();
            
            int expirados = 0;
            
            for (User user : usuarios) {
                if (user.getFechaExpiracionPassword() != null && 
                    user.getFechaExpiracionPassword().before(ahora) &&
                    !user.isRequiresPasswordChange()) {
                    
                    user.setRequiresPasswordChange(true);
                    userRepository.save(user);
                    expirados++;
                    
                    logger.info("Contraseña marcada como expirada para: {}", user.getEmail());
                }
            }
            
            logger.info("Verificación completada. {} contraseñas marcadas como expiradas", expirados);
            
        } catch (Exception e) {
            logger.error("Error en verificación de expiraciones: {}", e.getMessage());
        }
    }
}