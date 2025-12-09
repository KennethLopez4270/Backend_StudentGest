package com.studentgest.user_service.repository;

import com.studentgest.user_service.model.Funcionalidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionalidadRepository extends JpaRepository<Funcionalidad, Integer> {
}