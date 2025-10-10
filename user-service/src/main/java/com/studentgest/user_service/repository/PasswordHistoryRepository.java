package com.studentgest.user_service.repository;

import com.studentgest.user_service.model.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Integer> {
    
    // Encontrar historial de contraseñas por usuario, ordenado por fecha descendente
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    // Método corregido - usar @Query en lugar del nombre del método automático
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.userId = :userId ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findTop5ByUserId(@Param("userId") Integer userId);
    
    // Eliminar contraseñas antiguas (más de las últimas 5)
    @Modifying
    @Query("DELETE FROM PasswordHistory ph WHERE ph.userId = :userId AND ph.id NOT IN " +
           "(SELECT ph2.id FROM PasswordHistory ph2 WHERE ph2.userId = :userId ORDER BY ph2.createdAt DESC LIMIT 5)")
    void deleteOldPasswords(@Param("userId") Integer userId);
    
    // Contar cuántas contraseñas tiene un usuario en el historial
    long countByUserId(Integer userId);
}