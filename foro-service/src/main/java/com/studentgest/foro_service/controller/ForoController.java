package com.studentgest.foro_service.controller;

import com.studentgest.foro_service.dto.*;
import com.studentgest.foro_service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foro")
public class ForoController {

    @Autowired
    private ForoService foroService;

    @PostMapping
    public ResponseEntity<ForoResponse> crearForo(@RequestBody ForoRequest request) {
        return ResponseEntity.ok(foroService.crearForo(request));
    }

    @GetMapping
    public ResponseEntity<List<ForoResponse>> obtenerTodosLosForos() {
        return ResponseEntity.ok(foroService.obtenerTodosLosForos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForoResponse> obtenerForoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(foroService.obtenerForoPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ForoResponse> actualizarForo(
            @PathVariable Long id,
            @RequestBody ForoRequest request) {
        return ResponseEntity.ok(foroService.actualizarForo(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarForo(@PathVariable Long id) {
        foroService.eliminarForo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<ForoResponse>> obtenerForosPorCurso(
            @PathVariable Long cursoId) {
        List<ForoResponse> foros = foroService.obtenerForosPorCurso(cursoId);
        return ResponseEntity.ok(foros);
    }
}