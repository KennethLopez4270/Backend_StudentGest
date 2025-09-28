package com.estudentgest.student_service.controller;

import com.estudentgest.student_service.dto.EstudianteUsuarioDTO;
import com.estudentgest.student_service.dto.PadreDTO;
import com.estudentgest.student_service.model.PadreEstudiante;
import com.estudentgest.student_service.service.PadreEstudianteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class PadreEstudianteController {

    private final PadreEstudianteService padreEstudianteService;

    public PadreEstudianteController(PadreEstudianteService padreEstudianteService) {
        this.padreEstudianteService = padreEstudianteService;
    }

    // GET http://localhost:8080/api/students/hijos/{id_padre}
    @GetMapping("/hijos/{id_padre}")
    public ResponseEntity<?> obtenerEstudiantesPorPadre(@PathVariable Long id_padre) {
        try {
            List<EstudianteUsuarioDTO> estudiantes = padreEstudianteService.obtenerEstudiantesPorPadre(id_padre);
            if (estudiantes.isEmpty()) return ResponseEntity.ok(null);
            return ResponseEntity.ok(estudiantes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /padres/{id_estudiante}
    @GetMapping("/padres/{id_estudiante}")
    public ResponseEntity<List<PadreDTO>> obtenerPadresPorEstudiante(@PathVariable Long id_estudiante) {
        List<PadreDTO> padres = padreEstudianteService.obtenerPadresPorEstudiante(id_estudiante);
        if (padres.isEmpty()) return ResponseEntity.ok(null);
        return ResponseEntity.ok(padres);
    }

    // POST /api/students/conectar
    @PostMapping("/conectar")
    public ResponseEntity<?> conectarPadreEstudiante(@RequestBody PadreEstudiante padreEstudiante) {
        try {
            PadreEstudiante nuevaRelacion = padreEstudianteService.conectarPadreConEstudiante(padreEstudiante);
            return ResponseEntity.ok(nuevaRelacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
