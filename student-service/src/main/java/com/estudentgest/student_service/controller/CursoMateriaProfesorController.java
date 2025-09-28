package com.estudentgest.student_service.controller;

import com.estudentgest.student_service.dto.CursoMateriaEstudiantesDTO;
import com.estudentgest.student_service.dto.CursoMateriaProfesorDTO;
import com.estudentgest.student_service.model.CursoMateriaProfesor;
import com.estudentgest.student_service.service.CursoMateriaProfesorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class CursoMateriaProfesorController {

    private final CursoMateriaProfesorService service;

    @GetMapping("/curso_materia/{idProfesor}")
    public List<CursoMateriaProfesorDTO> getPorProfesor(@PathVariable Long idProfesor) {
        return service.getCursosYMateriasPorProfesor(idProfesor);
    }

    @GetMapping("/curso_materia_profesor")
    public List<CursoMateriaProfesor> getAll() {
        return service.getAll();
    }

    @GetMapping("/curso_materia/{idProfesor}/estudiantes")
    public ResponseEntity<List<CursoMateriaEstudiantesDTO>> getCursoMateriaConEstudiantes(@PathVariable Long idProfesor) {
        List<CursoMateriaEstudiantesDTO> datos = service.obtenerCursoMateriaConEstudiantes(idProfesor);
        return ResponseEntity.ok(datos);
    }
    @GetMapping("/curso_materia_profesor/{idCmp}")
    public ResponseEntity<CursoMateriaProfesor> getByIdCmp(@PathVariable Long idCmp) {
        return service.getByIdCmp(idCmp)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/curso_materia_profesor/{idCmp}/estudiantes")
    public ResponseEntity<CursoMateriaEstudiantesDTO> getEstudiantesPorCmp(@PathVariable Long idCmp) {
        CursoMateriaEstudiantesDTO dto = service.obtenerCursoMateriaConEstudiantesPorCmp(idCmp);
        return ResponseEntity.ok(dto);
    }

}
