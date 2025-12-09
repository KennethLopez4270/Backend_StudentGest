package com.studentgest.user_service.controller;

import com.studentgest.user_service.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificacionClientController {

    @Autowired
    private NotificacionService notificacionService;

    @PostMapping("/reset-password")
    public ResponseEntity<String> enviarNotificacionResetPassword(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String nombre = request.get("nombre");
        String nuevaPassword = request.get("nuevaPassword");

        notificacionService.enviarNotificacionResetPassword(to, nombre, nuevaPassword);
        return ResponseEntity.ok("Notificación de reseteo enviada.");
    }

    @PostMapping("/activate-account")
    public ResponseEntity<String> enviarNotificacionActivarCuenta(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String nombre = request.get("nombre");

        notificacionService.enviarNotificacionActivarCuenta(to, nombre);
        return ResponseEntity.ok("Notificación de activación enviada.");
    }
}