package com.studentgest.foro_service.controller;

import com.studentgest.foro_service.dto.*;
import com.studentgest.foro_service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foro/{foroId}") // Cambiamos la ruta base
public class MensajeForoController {

    @Autowired
    private ForoService foroService;

    @PostMapping("/comentar")
    public ResponseEntity<ForoResponse> agregarMensaje(
            @PathVariable Long foroId,
            @RequestBody MensajeRequest request) {
        return ResponseEntity.ok(foroService.agregarMensaje(foroId, request));
    }

    // Nuevo endpoint para obtener mensajes
    @GetMapping("/mensajes")
    public ResponseEntity<List<MensajeResponse>> obtenerMensajesDeForo(
            @PathVariable Long foroId) {
        List<MensajeResponse> mensajes = foroService.obtenerMensajesPorForo(foroId);
        return ResponseEntity.ok(mensajes);
    }
}