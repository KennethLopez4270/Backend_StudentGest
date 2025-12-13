package com.estudentgest.student_service.repository;

import com.estudentgest.student_service.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
