package com.studentgest.user_service.service;

import com.studentgest.user_service.model.PasswordHistory;
import com.studentgest.user_service.repository.PasswordHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PasswordHistoryService {
    
    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;
    
    private static final int MAX_PASSWORD_HISTORY = 5;
    
    /**
     * Verificar si la nueva contraseña ya fue usada anteriormente
     */
    public boolean isPasswordInHistory(Integer userId, String newPasswordHash) {
        List<PasswordHistory> passwordHistory = 
            passwordHistoryRepository.findTop5ByUserId(userId); // ← Método corregido
        
        return passwordHistory.stream()
                .anyMatch(ph -> ph.getPasswordHash().equals(newPasswordHash));
    }
    
    /**
     * Agregar nueva contraseña al historial
     */
    public void addToPasswordHistory(Integer userId, String passwordHash) {
        // Crear nuevo registro de historial
        PasswordHistory newHistory = new PasswordHistory(userId, passwordHash);
        passwordHistoryRepository.save(newHistory);
        
        // Limpiar contraseñas antiguas (mantener solo las últimas 5)
        cleanOldPasswords(userId);
    }
    
    /**
     * Limpiar contraseñas antiguas, mantener solo las últimas MAX_PASSWORD_HISTORY
     */
    private void cleanOldPasswords(Integer userId) {
        List<PasswordHistory> allPasswords = 
            passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        if (allPasswords.size() > MAX_PASSWORD_HISTORY) {
            // Eliminar las más antiguas (mantener solo las primeras 5)
            List<PasswordHistory> passwordsToDelete = allPasswords.subList(MAX_PASSWORD_HISTORY, allPasswords.size());
            passwordHistoryRepository.deleteAll(passwordsToDelete);
        }
    }
    
    /**
     * Obtener el historial de contraseñas de un usuario
     */
    public List<PasswordHistory> getPasswordHistory(Integer userId) {
        return passwordHistoryRepository.findTop5ByUserId(userId); // ← Método corregido
    }
}