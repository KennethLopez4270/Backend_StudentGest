package com.studentgest.notificaciones.controller;

import com.studentgest.notificaciones.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

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
}

