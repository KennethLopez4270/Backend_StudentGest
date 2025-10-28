package com.studentgest.user_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificacionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String notificacionesUrl = "http://localhost:8081/api/email/send";

    public void enviarNotificacionResetPassword(String to, String nombre, String nuevaPassword) {
        String asunto = "Reseteo de contraseña";
        String cuerpo = String.format("Hola %s,\n\nTu nueva contraseña temporal es: %s\n\nTe recomendamos cambiarla al iniciar sesión.", nombre, nuevaPassword);
        enviarNotificacion(to, asunto, cuerpo);
    }

    public void enviarNotificacionActivarCuenta(String to, String nombre) {
        String asunto = "Activación de cuenta";
        String cuerpo = String.format("Hola %s,\n\nTu cuenta ha sido activada con éxito. Ahora puedes iniciar sesión.", nombre);
        enviarNotificacion(to, asunto, cuerpo);
    }

    private void enviarNotificacion(String to, String subject, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("to", to);
        body.put("subject", subject);
        body.put("message", message);

        restTemplate.postForObject(notificacionesUrl, body, String.class);
    }
}