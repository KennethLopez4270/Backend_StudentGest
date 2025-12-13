package com.estudentgest.student_service.repository;

import com.estudentgest.student_service.model.PadreEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PadreEstudianteRepository extends JpaRepository<PadreEstudiante, Long> {
    List<PadreEstudiante> findByIdPadre(Long idPadre);
    List<PadreEstudiante> findByIdEstudiante(Long idEstudiante);
    boolean existsByIdPadreAndIdEstudiante(Long idPadre, Long idEstudiante);
}

