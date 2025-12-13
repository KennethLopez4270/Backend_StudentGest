package com.estudentgest.student_service.controller;

import com.estudentgest.student_service.dto.CursoDTO;
import com.estudentgest.student_service.model.EstudianteCurso;
import com.estudentgest.student_service.service.EstudianteCursoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class EstudianteCursoController {

    private final EstudianteCursoService estudianteCursoService;

    public EstudianteCursoController(EstudianteCursoService estudianteCursoService) {
        this.estudianteCursoService = estudianteCursoService;
    }

    // Obtener estudiantes por curso
    @GetMapping("/estudiantes/{id_curso}")
    public List<EstudianteCurso> getEstudiantesPorCurso(@PathVariable("id_curso") Long idCurso) {
        return estudianteCursoService.obtenerEstudiantesPorCurso(idCurso);
    }

    // Obtener cursos por estudiante
    @GetMapping("/cursos/{id_estudiante}")
    public List<CursoDTO> getCursosPorEstudiante(@PathVariable("id_estudiante") Long idEstudiante) {
        return estudianteCursoService.obtenerCursosPorEstudiante(idEstudiante);
    }
}
