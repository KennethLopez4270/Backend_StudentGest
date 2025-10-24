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
        String cuerpo = String.format("Hola %s,\n\nTu nueva contraseña temporal es: %s\n\nTe recomendamos cambiarla al iniciar sesión.", nombre, nuevaPassword);

        Map<String, String> body = new HashMap<>();
        body.put("to", to);
        body.put("subject", asunto);
        body.put("message", cuerpo);

        restTemplate.postForObject(notificacionesUrl, body, String.class);
    }
}
