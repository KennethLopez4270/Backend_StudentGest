package com.studentgest.notificaciones.repository;

import com.studentgest.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByEstadoAndFechaEnvioProgramadoBefore(String estado, LocalDateTime fecha);
    List<Notificacion> findByEsRecurrenteTrue();


}

