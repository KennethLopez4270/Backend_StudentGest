package com.studentgest.notificaciones.controller;


import com.studentgest.notificaciones.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public String enviarCorreoPersonalizado(@RequestBody Map<String, String> datos) {
        try {
            emailService.enviarEmailSimple(
                    datos.get("to"),
                    datos.get("subject"),
                    datos.get("message")
            );
            return "Correo enviado correctamente";
        } catch (Exception e) {
            return "Error al enviar: " + e.getMessage();
        }
    }
}

