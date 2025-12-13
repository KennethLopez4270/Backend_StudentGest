package com.studentgest.notificaciones.service;


import com.studentgest.notificaciones.dto.NotificacionAsignacionDTO;
import com.studentgest.notificaciones.dto.NotificacionDTO;
import com.studentgest.notificaciones.model.Notificacion;
import com.studentgest.notificaciones.model.UsuarioNotificacion;
import com.studentgest.notificaciones.repository.NotificacionRepository;
import com.studentgest.notificaciones.repository.UsuarioNotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioNotificacionRepository usuarioNotificacionRepository;

    @Autowired
    private EmailService emailService;


    public List<Notificacion> obtenerTodas() {
        return notificacionRepository.findAll();
    }

    public Notificacion crear(NotificacionDTO notificacionDTO) {
        Notificacion notificacion = new Notificacion();
        notificacion.setTitulo(notificacionDTO.getTitulo());
        notificacion.setMensaje(notificacionDTO.getMensaje());
        notificacion.setCanal(notificacionDTO.getCanal());
        notificacion.setEstado("pendiente");
        return notificacionRepository.save(notificacion);
    }

    public Notificacion crearYAsignar(NotificacionAsignacionDTO dto) {
        Notificacion notificacion = new Notificacion();
        notificacion.setTitulo(dto.getTitulo());
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setCanal(dto.getCanal());
        notificacion.setEsRecurrente(dto.isEsRecurrente());
        notificacion.setFrecuencia(dto.getFrecuencia());
        notificacion.setEstado("pendiente");

        Notificacion guardada = notificacionRepository.save(notificacion);

        for (Long idUsuario : dto.getIdsUsuarios()) {
            UsuarioNotificacion usuarioNotif = new UsuarioNotificacion();
            usuarioNotif.setIdUsuario(idUsuario);
            usuarioNotif.setNotificacion(guardada);
            usuarioNotif.setEstado("pendiente");
            usuarioNotificacionRepository.save(usuarioNotif);
        }

        return guardada;
    }

    public List<UsuarioNotificacion> obtenerPorUsuario(Long idUsuario) {
        return usuarioNotificacionRepository.findByIdUsuario(idUsuario);
    }

    public void marcarComoVista(Long idUsuarioNotificacion) {
        UsuarioNotificacion notificacion = usuarioNotificacionRepository.findById(idUsuarioNotificacion).orElseThrow();
        notificacion.setEstado("vista");
        notificacion.setFechaVista(LocalDateTime.now());
        usuarioNotificacionRepository.save(notificacion);
    }
    public void procesarNotificacionesProgramadas() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Notificacion> pendientes = notificacionRepository
                .findByEstadoAndFechaEnvioProgramadoBefore("pendiente", ahora);

        for (Notificacion notif : pendientes) {
            List<UsuarioNotificacion> destinos = usuarioNotificacionRepository
                    .findByNotificacionId(notif.getId());

            for (UsuarioNotificacion un : destinos) {
                // Simulación: deberías obtener el email real del usuario por su ID
                String email = obtenerEmailPorUsuario(un.getIdUsuario());
                if (notif.getCanal().equalsIgnoreCase("email") && email != null) {
                    emailService.enviarEmailSimple(
                            email,
                            notif.getTitulo(),
                            notif.getMensaje()
                    );
                }
            }

            notif.setEstado("enviada");
            notificacionRepository.save(notif);
        }
    }
    private String obtenerEmailPorUsuario(Long idUsuario) {
        // Este método debe integrarse con el servicio de usuarios (SOA).
        // Por ahora usaremos valores fijos simulados para pruebas:
        if (idUsuario == 1L) return "padre1@gmail.com";
        if (idUsuario == 2L) return "profesor1@gmail.com";
        if (idUsuario == 3L) return "estudiante1@gmail.com";
        return "otro@gmail.com";
    }
    public List<Notificacion> obtenerRecurrentes() {
        return notificacionRepository.findByEsRecurrenteTrue();
    }

    public void generarCopiaProgramada(Notificacion base, LocalDateTime nuevaFecha) {
        Notificacion copia = new Notificacion();
        copia.setTitulo(base.getTitulo());
        copia.setMensaje(base.getMensaje());
        copia.setCanal(base.getCanal());
        copia.setEstado("pendiente");
        copia.setFechaEnvioProgramado(nuevaFecha);
        copia.setEsRecurrente(false); // la copia no se sigue repitiendo
        notificacionRepository.save(copia);

        List<UsuarioNotificacion> destinos = usuarioNotificacionRepository.findByNotificacionId(base.getId());

        for (UsuarioNotificacion un : destinos) {
            UsuarioNotificacion nueva = new UsuarioNotificacion();
            nueva.setIdUsuario(un.getIdUsuario());
            nueva.setNotificacion(copia);
            nueva.setEstado("pendiente");
            usuarioNotificacionRepository.save(nueva);
        }
    }
    private LocalDateTime calcularProxima(LocalDateTime base, String frecuencia) {
        switch (frecuencia.toLowerCase()) {
            case "diaria":
                return base.plusDays(1);
            case "semanal":
                return base.plusWeeks(1);
            case "mensual":
                return base.plusMonths(1);
            default:
                return base;
        }
    }



}
