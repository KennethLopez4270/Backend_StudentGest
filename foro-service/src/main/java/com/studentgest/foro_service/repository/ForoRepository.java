package com.studentgest.foro_service.repository;

import com.studentgest.foro_service.model.Foro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForoRepository extends JpaRepository<Foro, Long> {
    List<Foro> findByCursoId(Long cursoId);
}