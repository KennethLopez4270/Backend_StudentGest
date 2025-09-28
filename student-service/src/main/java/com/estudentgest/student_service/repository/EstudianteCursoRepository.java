package com.estudentgest.student_service.repository;

import com.estudentgest.student_service.model.EstudianteCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstudianteCursoRepository extends JpaRepository<EstudianteCurso, Long> {
    List<EstudianteCurso> findByIdCurso(Long idCurso);
    List<EstudianteCurso> findByIdEstudiante(Long idEstudiante);
}
