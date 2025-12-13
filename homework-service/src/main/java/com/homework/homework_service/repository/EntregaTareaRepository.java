package com.homework.homework_service.repository;

import com.homework.homework_service.model.EntregaTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntregaTareaRepository extends JpaRepository<EntregaTarea, Long> {
}

