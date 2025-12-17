package com.estudentgest.student_service.controller;

import com.estudentgest.student_service.dto.CursoMateriaEstudiantesDTO;
import com.estudentgest.student_service.dto.CursoMateriaProfesorDTO;
import com.estudentgest.student_service.model.CursoMateriaProfesor;
import com.estudentgest.student_service.service.CursoMateriaProfesorService;
import com.estudentgest.student_service.service.UserActivityLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class CursoMateriaProfesorController {

    private final CursoMateriaProfesorService service;

    @Autowired
    private UserActivityLogger activityLogger;

    @GetMapping("/curso_materia/{idProfesor}")
    public List<CursoMateriaProfesorDTO> getPorProfesor(@PathVariable Long idProfesor) {
        List<CursoMateriaProfesorDTO> cursos = service.getCursosYMateriasPorProfesor(idProfesor);

        // Log de actividad
        activityLogger.logCoursesViewed(idProfesor, cursos.size());

        return cursos;
    }

    @GetMapping("/curso_materia_profesor")
    public List<CursoMateriaProfesor> getAll() {
        List<CursoMateriaProfesor> all = service.getAll();

        // Log de actividad
        activityLogger.logActivity("TODOS_CURSOS_CONSULTADOS", "Total: " + all.size(), true);

        return all;
    }

    @GetMapping("/curso_materia/{idProfesor}/estudiantes")
    public ResponseEntity<List<CursoMateriaEstudiantesDTO>> getCursoMateriaConEstudiantes(
            @PathVariable Long idProfesor) {
        List<CursoMateriaEstudiantesDTO> datos = service.obtenerCursoMateriaConEstudiantes(idProfesor);

        // Log de actividad
        activityLogger.logActivity(
                "CURSOS_CON_ESTUDIANTES_CONSULTADOS",
                "Profesor: " + idProfesor + " | Cursos: " + datos.size(),
                true);

        return ResponseEntity.ok(datos);
    }

    @GetMapping("/curso_materia_profesor/{idCmp}")
    public ResponseEntity<CursoMateriaProfesor> getByIdCmp(@PathVariable Long idCmp) {
        return service.getByIdCmp(idCmp)
                .map(cmp -> {
                    // Log de actividad
                    activityLogger.logCourseViewed(idCmp);
                    return ResponseEntity.ok(cmp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/curso_materia_profesor/{idCmp}/estudiantes")
    public ResponseEntity<CursoMateriaEstudiantesDTO> getEstudiantesPorCmp(@PathVariable Long idCmp) {
        CursoMateriaEstudiantesDTO dto = service.obtenerCursoMateriaConEstudiantesPorCmp(idCmp);

        // Log de actividad
        int estudiantesCount = dto.getEstudiantes() != null ? dto.getEstudiantes().size() : 0;
        activityLogger.logStudentsViewed(idCmp, estudiantesCount);

        return ResponseEntity.ok(dto);
    }
}
