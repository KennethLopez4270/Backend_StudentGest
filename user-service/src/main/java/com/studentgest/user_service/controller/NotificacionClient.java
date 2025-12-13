package com.studentgest.user_service.controller;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificacionClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String notificacionesUrl = "http://localhost:8085/api/email/send";

    public void enviarNotificacionResetPassword(String to, String nombre, String nuevaPassword) {
        String asunto = "Reseteo de contraseña";
        String cuerpo = String.format(
                "Hola %s,\n\nTu nueva contraseña temporal es: %s\n\nTe recomendamos cambiarla al iniciar sesión.",
                nombre, nuevaPassword);
        enviarEmail(to, asunto, cuerpo);
    }

    public void enviarNotificacionAprobacion(String to, String nombre) {
        String asunto = "Cuenta Aprobada";
        String cuerpo = String.format(
                "Hola %s,\n\n¡Felicidades! Tu cuenta ha sido aprobada administrativamente. Ya puedes acceder al sistema con normalidad.",
                nombre);
        enviarEmail(to, asunto, cuerpo);
    }

    public void enviarNotificacionRechazo(String to, String nombre) {
        String asunto = "Cuenta Rechazada";
        String cuerpo = String.format(
                "Hola %s,\n\nLo sentimos, tu solicitud de cuenta ha sido rechazada. Por favor contacta al administrador para más información.",
                nombre);
        enviarEmail(to, asunto, cuerpo);
    }

    public void enviarNotificacionActivacion(String to, String nombre) {
        String asunto = "Cuenta Activada";
        String cuerpo = String.format("Hola %s,\n\nTu cuenta ha sido reactivada exitosamente. Bienvenido de vuelta.",
                nombre);
        enviarEmail(to, asunto, cuerpo);
    }

    private void enviarEmail(String to, String subject, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("to", to);
        body.put("subject", subject);
        body.put("message", message);
        restTemplate.postForObject(notificacionesUrl, body, String.class);
    }
}
