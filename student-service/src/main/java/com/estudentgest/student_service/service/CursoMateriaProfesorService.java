package com.estudentgest.student_service.service;

import com.estudentgest.student_service.dto.CursoMateriaEstudiantesDTO;
import com.estudentgest.student_service.dto.CursoMateriaProfesorDTO;
import com.estudentgest.student_service.dto.UsuarioDTO;
import com.estudentgest.student_service.model.Curso;
import com.estudentgest.student_service.model.CursoMateriaProfesor;
import com.estudentgest.student_service.model.Estudiante;
import com.estudentgest.student_service.model.EstudianteCurso;
import com.estudentgest.student_service.model.Materia;
import com.estudentgest.student_service.repository.CursoMateriaProfesorRepository;
import com.estudentgest.student_service.repository.CursoRepository;
import com.estudentgest.student_service.repository.EstudianteCursoRepository;
import com.estudentgest.student_service.repository.MateriaRepository;
import com.estudentgest.student_service.repository.EstudianteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CursoMateriaProfesorService {

    private final CursoMateriaProfesorRepository cmpRepository;
    private final CursoRepository cursoRepository;
    private final MateriaRepository materiaRepository;
    private final EstudianteCursoRepository estudianteCursoRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioClient usuarioClient;


    public List<CursoMateriaProfesorDTO> getCursosYMateriasPorProfesor(Long idProfesor) {
        List<CursoMateriaProfesor> relaciones = cmpRepository.findByIdProfesor(idProfesor);
        List<CursoMateriaProfesorDTO> respuesta = new ArrayList<>();

        for (CursoMateriaProfesor cmp : relaciones) {
            Optional<Curso> cursoOpt = cursoRepository.findById(cmp.getIdCurso());
            Optional<Materia> materiaOpt = materiaRepository.findById(cmp.getIdMateria());

            if (cursoOpt.isPresent() && materiaOpt.isPresent()) {
                Curso curso = cursoOpt.get();
                Materia materia = materiaOpt.get();

                List<EstudianteCurso> estudiantesCurso = estudianteCursoRepository.findByIdCurso(curso.getId_curso());
                List<String> nombresEstudiantes = estudiantesCurso.stream()
                        .map(ec -> estudianteRepository.findById(ec.getIdEstudiante()))
                        .filter(Optional::isPresent)
                        .map(est -> {
                            Estudiante e = est.get();
                            UsuarioDTO user = usuarioClient.getUsuarioById(e.getIdUsuario());
                            return user != null ? user.getNombre() + " " + user.getApellido_paterno() : "Estudiante desconocido";
                        })
                        .collect(Collectors.toList());

                respuesta.add(new CursoMateriaProfesorDTO(
                        cmp.getIdCmp(),
                        curso.getNombre(),
                        curso.getNivel(),
                        curso.getTurno(),
                        curso.getGestion(),
                        materia.getNombre(),
                        nombresEstudiantes));
            }
        }

        return respuesta;
    }

    public List<CursoMateriaProfesor> getAll() {
        return cmpRepository.findAll();
    }

    public List<CursoMateriaEstudiantesDTO> obtenerCursoMateriaConEstudiantes(Long idProfesor) {
        List<CursoMateriaProfesor> relaciones = cmpRepository.findByIdProfesor(idProfesor);
        List<CursoMateriaEstudiantesDTO> resultado = new ArrayList<>();

        for (CursoMateriaProfesor cmp : relaciones) {
            Curso curso = cursoRepository.findById(cmp.getIdCurso()).orElse(null);
            Materia materia = materiaRepository.findById(cmp.getIdMateria()).orElse(null);

            if (curso != null && materia != null) {
                List<EstudianteCurso> ecList = estudianteCursoRepository.findByIdCurso(curso.getId_curso());

                // Obtener IDs de los estudiantes directamente
                List<Long> idsEstudiantes = ecList.stream()
                        .map(EstudianteCurso::getIdEstudiante)
                        .distinct()
                        .toList();

                resultado.add(new CursoMateriaEstudiantesDTO(
                        curso.getNombre(),
                        curso.getNivel(),
                        curso.getTurno(),
                        curso.getGestion(),
                        materia.getNombre(),
                        idsEstudiantes
                ));
            }
        }

        return resultado;
    }

    public CursoMateriaEstudiantesDTO obtenerCursoMateriaConEstudiantesPorCmp(Long idCmp) {
        CursoMateriaProfesor cmp = cmpRepository.findById(idCmp)
                .orElseThrow(() -> new RuntimeException("Curso-Materia-Profesor no encontrado"));

        Curso curso = cursoRepository.findById(cmp.getIdCurso()).orElse(null);
        Materia materia = materiaRepository.findById(cmp.getIdMateria()).orElse(null);

        if (curso != null && materia != null) {
            List<EstudianteCurso> ecList = estudianteCursoRepository.findByIdCurso(curso.getId_curso());

            List<Long> idsEstudiantes = ecList.stream()
                    .map(EstudianteCurso::getIdEstudiante)
                    .distinct()
                    .toList();

            return new CursoMateriaEstudiantesDTO(
                    curso.getNombre(),
                    curso.getNivel(),
                    curso.getTurno(),
                    curso.getGestion(),
                    materia.getNombre(),
                    idsEstudiantes
            );
        } else {
            throw new RuntimeException("Curso o Materia no encontrados para el CMP indicado");
        }
    }

    public Optional<CursoMateriaProfesor> getByIdCmp(Long idCmp) {
        return cmpRepository.findById(idCmp);
    }

}
