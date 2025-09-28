package com.studentgest.notificaciones.scheduler;

import com.studentgest.notificaciones.model.Notificacion;
import com.studentgest.notificaciones.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificacionScheduler {

    @Autowired
    private NotificacionService notificacionService;

    // Ejecuta cada minuto
    @Scheduled(fixedRate = 60000)
    public void revisarNotificacionesProgramadas() {
        notificacionService.procesarNotificacionesProgramadas();
    }
    @Scheduled(fixedRate = 60000) // cada minuto
    public void generarNuevasInstanciasRecurrentes() {
        List<Notificacion> recurrentes = notificacionService.obtenerRecurrentes();

        for (Notificacion base : recurrentes) {
            if (base.getFechaEnvioProgramado() == null) continue;

            LocalDateTime proximaFecha = calcularProxima(base.getFechaEnvioProgramado(), base.getFrecuencia());

            // Evita crear duplicados
            if (proximaFecha.isBefore(LocalDateTime.now().plusMinutes(1))) {
                notificacionService.generarCopiaProgramada(base, proximaFecha);
            }
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

