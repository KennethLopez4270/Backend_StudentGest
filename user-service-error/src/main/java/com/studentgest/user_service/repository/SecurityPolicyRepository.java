package com.studentgest.user_service.repository;

import com.studentgest.user_service.model.SecurityPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityPolicyRepository extends JpaRepository<SecurityPolicy, Integer> {
    
    Optional<SecurityPolicy> findByNombrePolitica(String nombrePolitica);
    
    List<SecurityPolicy> findByCategoria(String categoria);
    
    List<SecurityPolicy> findByEditable(Boolean editable);
    
    @Query("SELECT sp FROM SecurityPolicy sp WHERE sp.categoria = :categoria AND sp.editable = true")
    List<SecurityPolicy> findEditableByCategoria(@Param("categoria") String categoria);
    
    boolean existsByNombrePolitica(String nombrePolitica);
}