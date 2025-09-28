package com.estudentgest.student_service.repository;

import com.estudentgest.student_service.model.CursoMateriaProfesor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoMateriaProfesorRepository extends JpaRepository<CursoMateriaProfesor, Long> {
    List<CursoMateriaProfesor> findByIdProfesor(Long idProfesor);
}
