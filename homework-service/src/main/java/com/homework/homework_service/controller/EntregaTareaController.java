package com.homework.homework_service.controller;

import com.homework.homework_service.model.EntregaTarea;
import com.homework.homework_service.repository.EntregaTareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/homework/entregas")
@RequiredArgsConstructor
public class EntregaTareaController {

    private final EntregaTareaRepository entregaTareaRepository;

    // Obtener todas las entregas
    @GetMapping
    public List<EntregaTarea> getAllEntregas() {
        return entregaTareaRepository.findAll();
    }

    // Obtener una entrega por ID
    @GetMapping("/{id}")
    public ResponseEntity<EntregaTarea> getEntregaById(@PathVariable Long id) {
        return entregaTareaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Editar una entrega
    @PutMapping("/{id}")
    public ResponseEntity<EntregaTarea> updateEntrega(@PathVariable Long id, @RequestBody EntregaTarea entregaActualizada) {
        return entregaTareaRepository.findById(id)
                .map(entrega -> {
                    entrega.setEstado(entregaActualizada.getEstado());
                    entrega.setCalificacion(entregaActualizada.getCalificacion());
                    entrega.setComentario(entregaActualizada.getComentario());
                    entrega.setFechaEntrega(entregaActualizada.getFechaEntrega());
                    return ResponseEntity.ok(entregaTareaRepository.save(entrega));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
