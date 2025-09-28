package com.estudentgest.student_service.controller;

import com.estudentgest.student_service.dto.EstudianteUsuarioDTO;
import com.estudentgest.student_service.model.Estudiante;
import com.estudentgest.student_service.service.EstudianteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public List<Estudiante> obtenerEstudiantes() {
        return estudianteService.obtenerTodosLosEstudiantes();
    }

    @GetMapping("/estudiante_usuario")
    public ResponseEntity<List<EstudianteUsuarioDTO>> getEstudiantesConUsuario() {
        List<EstudianteUsuarioDTO> estudiantes = estudianteService.getEstudiantesConUsuario();
        return ResponseEntity.ok(estudiantes);
    }

    @GetMapping("/estudiante_usuario/{id_estudiante}")
    public ResponseEntity<?> getEstudianteUsuarioByIdEstudiante(@PathVariable Long id_estudiante) {
        EstudianteUsuarioDTO dto = estudianteService.getEstudianteUsuarioByIdEstudiante(id_estudiante);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Estudiante no encontrado");
        }
    }

    @GetMapping("/estudiante_usuario/usuario/{id_usuario}")
    public ResponseEntity<?> getEstudianteUsuarioByIdUsuario(@PathVariable Long id_usuario) {
        EstudianteUsuarioDTO dto = estudianteService.getEstudianteUsuarioByIdUsuario(id_usuario);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No es estudiante");
        }
    }

}
