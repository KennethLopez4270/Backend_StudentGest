package com.estudentgest.student_service.repository;

import com.estudentgest.student_service.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Estudiante findByIdUsuario(Long idUsuario);

}
