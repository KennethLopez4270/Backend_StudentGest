package com.studentgest.foro_service.repository;

import com.studentgest.foro_service.model.MensajeForo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeForoRepository extends JpaRepository<MensajeForo, Long> {
    List<MensajeForo> findByForoId(Long foroId);
    List<MensajeForo> findByForoIdOrderByFechaDesc(Long foroId);
}