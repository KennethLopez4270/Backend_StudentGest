package com.estudentgest.student_service.repository;

import com.estudentgest.student_service.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
}
