package com.estudentgest.student_service.service;

import com.estudentgest.student_service.dto.CursoDTO;
import com.estudentgest.student_service.model.Curso;
import com.estudentgest.student_service.model.EstudianteCurso;
import com.estudentgest.student_service.repository.CursoRepository;
import com.estudentgest.student_service.repository.EstudianteCursoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EstudianteCursoService {

    private final EstudianteCursoRepository estudianteCursoRepository;
    private final CursoRepository cursoRepository;

    public EstudianteCursoService(EstudianteCursoRepository estudianteCursoRepository, CursoRepository cursoRepository) {
        this.estudianteCursoRepository = estudianteCursoRepository;
        this.cursoRepository = cursoRepository;
    }

    public List<EstudianteCurso> obtenerEstudiantesPorCurso(Long idCurso) {
        return estudianteCursoRepository.findByIdCurso(idCurso);
    }

    public List<CursoDTO> obtenerCursosPorEstudiante(Long idEstudiante) {
        List<EstudianteCurso> relaciones = estudianteCursoRepository.findByIdEstudiante(idEstudiante);
        List<CursoDTO> cursos = new ArrayList<>();

        for (EstudianteCurso ec : relaciones) {
            Curso curso = cursoRepository.findById(ec.getIdCurso()).orElse(null);
            if (curso != null) {
                CursoDTO dto = new CursoDTO();
                dto.setId_curso(curso.getId_curso());
                dto.setNombre(curso.getNombre());
                dto.setNivel(curso.getNivel());
                dto.setTurno(curso.getTurno());
                dto.setGestion(curso.getGestion());

                cursos.add(dto);
            }
        }

        return cursos;
    }
}
