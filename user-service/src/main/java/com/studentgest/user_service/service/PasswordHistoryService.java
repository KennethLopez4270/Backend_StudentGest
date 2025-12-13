package com.studentgest.user_service.service;

import com.studentgest.user_service.model.PasswordHistory;
import com.studentgest.user_service.repository.PasswordHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PasswordHistoryService {
    
    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final int MAX_PASSWORD_HISTORY = 5;
    
    /**
     * Verificar si la nueva contraseña ya fue usada anteriormente
     * USANDO passwordEncoder.matches para comparar correctamente
     */
    public boolean isPasswordInHistory(Integer userId, String plainPassword) {
        try {
            // Obtener las últimas 5 contraseñas del historial
            List<PasswordHistory> passwordHistory = 
                passwordHistoryRepository.findTop5ByUserId(userId);
            
            // Verificar si alguna coincide con la nueva contraseña usando matches
            boolean isInHistory = passwordHistory.stream()
                    .anyMatch(ph -> passwordEncoder.matches(plainPassword, ph.getPasswordHash()));
            
            System.out.println(" Verificando historial para usuario " + userId + 
                             ": " + (isInHistory ? "ENCONTRADA en historial" : "NO encontrada en historial"));
            System.out.println("Historial revisado: " + passwordHistory.size() + " contraseñas");
            
            return isInHistory;
            
        } catch (Exception e) {
            System.err.println(" Error verificando historial de contraseñas: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Agregar nueva contraseña al historial
     */
    public void addToPasswordHistory(Integer userId, String passwordHash) {
        try {
            // Crear nuevo registro de historial
            PasswordHistory newHistory = new PasswordHistory(userId, passwordHash);
            passwordHistoryRepository.save(newHistory);
            
            System.out.println("Contraseña agregada al historial para usuario: " + userId);
            
            // Limpiar contraseñas antiguas (mantener solo las últimas 5)
            cleanOldPasswords(userId);
            
        } catch (Exception e) {
            System.err.println("Error agregando al historial: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Limpiar contraseñas antiguas, mantener solo las últimas MAX_PASSWORD_HISTORY
     */
    private void cleanOldPasswords(Integer userId) {
        try {
            List<PasswordHistory> allPasswords = 
                passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            if (allPasswords.size() > MAX_PASSWORD_HISTORY) {
                // Eliminar las más antiguas (mantener solo las primeras 5)
                List<PasswordHistory> passwordsToDelete = allPasswords.subList(MAX_PASSWORD_HISTORY, allPasswords.size());
                passwordHistoryRepository.deleteAll(passwordsToDelete);
                
                System.out.println(" Limpiadas " + passwordsToDelete.size() + 
                                 " contraseñas antiguas del historial para usuario: " + userId);
            }
        } catch (Exception e) {
            System.err.println(" Error limpiando historial: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtener el historial de contraseñas de un usuario
     */
    public List<PasswordHistory> getPasswordHistory(Integer userId) {
        return passwordHistoryRepository.findTop5ByUserId(userId);
    }
    
    /**
     * Verificar si el usuario tiene historial de contraseñas
     */
    public boolean hasPasswordHistory(Integer userId) {
        return passwordHistoryRepository.countByUserId(userId) > 0;
    }
}