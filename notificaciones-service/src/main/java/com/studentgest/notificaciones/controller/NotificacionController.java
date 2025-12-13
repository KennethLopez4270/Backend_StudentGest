package com.studentgest.notificaciones.controller;


import com.studentgest.notificaciones.dto.NotificacionAsignacionDTO;
import com.studentgest.notificaciones.dto.NotificacionDTO;
import com.studentgest.notificaciones.model.Notificacion;
import com.studentgest.notificaciones.model.UsuarioNotificacion;
import com.studentgest.notificaciones.service.EmailService;
import com.studentgest.notificaciones.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;
    @Autowired
    private EmailService emailService;

    @GetMapping
    public List<Notificacion> listarNotificaciones() {
        return notificacionService.obtenerTodas();
    }


    @PostMapping
    public Notificacion crearYAsignar(@RequestBody NotificacionAsignacionDTO dto) {
        return notificacionService.crearYAsignar(dto);
    }

    @GetMapping("/usuario/{id}")
    public List<UsuarioNotificacion> listarPorUsuario(@PathVariable("id") Long idUsuario) {
        return notificacionService.obtenerPorUsuario(idUsuario);
    }

    @PutMapping("/usuario/marcar-vista/{idUsuarioNotificacion}")
    public void marcarComoVista(@PathVariable Long idUsuarioNotificacion) {
        notificacionService.marcarComoVista(idUsuarioNotificacion);
    }

    @GetMapping("/test")
    public String enviarCorreoDePrueba(@RequestParam("to") String destinatario) {
        try {
            emailService.enviarEmailSimple(
                    destinatario,
                    "Prueba de servicio",
                    "Servicio de correos OK"
            );
            return "Correo de prueba enviado a: " + destinatario;
        } catch (Exception e) {
            return "Error al enviar el correo: " + e.getMessage();
        }
    }

    @PostMapping("/attendance-change")
    public String notificarCambioAsistencia(@RequestBody Map<String, String> datos) {
        // Datos esperados: to, nombreEstudiante, fecha, nuevoTipo
        String mensaje = String.format("Se ha registrado un cambio en la asistencia de %s para la fecha %s. Nuevo estado: %s.",
                datos.get("nombreEstudiante"), datos.get("fecha"), datos.get("nuevoTipo"));

        emailService.enviarEmailSimple(datos.get("to"), "Cambio en asistencia", mensaje);
        return "Notificación enviada";
    }

    @PostMapping("/upcoming-tasks")
    public String notificarTareasPendientes(@RequestBody Map<String, Object> datos) {
        String to = (String) datos.get("to");
        List<String> tareas = (List<String>) datos.get("tareas");

        StringBuilder mensaje = new StringBuilder("Estas son tus tareas próximas a vencer:\n");
        for (String tarea : tareas) {
            mensaje.append("- ").append(tarea).append("\n");
        }

        emailService.enviarEmailSimple(to, "Tareas próximas a vencer", mensaje.toString());
        return "Correo enviado";
    }

    @PostMapping("/event")
    public String notificarEvento(@RequestBody Map<String, String> datos) {
        String mensaje = String.format("Te invitamos a participar del evento \"%s\" que se llevará a cabo el %s. Más información: %s",
                datos.get("titulo"), datos.get("fecha"), datos.get("descripcion"));

        emailService.enviarEmailSimple(datos.get("to"), "Nuevo evento escolar", mensaje);
        return "Notificación de evento enviada";
    }

    @PostMapping("/send-bulk")
    public String enviarCorreoMasivo(@RequestBody Map<String, Object> datos) {
        List<String> destinatarios = (List<String>) datos.get("to");
        String asunto = (String) datos.get("subject");
        String mensaje = (String) datos.get("message");

        for (String correo : destinatarios) {
            emailService.enviarEmailSimple(correo, asunto, mensaje);
        }
        return "Correos enviados";
    }


}

