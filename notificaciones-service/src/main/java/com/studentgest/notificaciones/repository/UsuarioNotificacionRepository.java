package com.studentgest.notificaciones.repository;

import com.studentgest.notificaciones.model.UsuarioNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioNotificacionRepository extends JpaRepository<UsuarioNotificacion, Long> {
    List<UsuarioNotificacion> findByIdUsuario(Long idUsuario);
    List<UsuarioNotificacion> findByNotificacionId(Long idNotificacion);

}
