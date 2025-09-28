package com.estudentgest.student_service.service;

import com.estudentgest.student_service.dto.CursoDTO;
import com.estudentgest.student_service.model.Curso;
import com.estudentgest.student_service.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    public List<Curso> obtenerTodosLosCursos() {
        return cursoRepository.findAll();
    }

    public Optional<Curso> obtenerCursoPorId(Long id) {
        return cursoRepository.findById(id);
    }

    public Curso crearCurso(CursoDTO cursoDTO) {
        Curso curso = new Curso();
        curso.setNombre(cursoDTO.getNombre());
        curso.setNivel(cursoDTO.getNivel());
        curso.setTurno(cursoDTO.getTurno());
        curso.setGestion(cursoDTO.getGestion());
        curso.setId_institucion(1); // valor por defecto
        return cursoRepository.save(curso);
    }

    public Optional<Curso> actualizarCurso(Long id, CursoDTO cursoDTO) {
        return cursoRepository.findById(id).map(curso -> {
            curso.setNombre(cursoDTO.getNombre());
            curso.setNivel(cursoDTO.getNivel());
            curso.setTurno(cursoDTO.getTurno());
            curso.setGestion(cursoDTO.getGestion());
            return cursoRepository.save(curso);
        });
    }

    public void eliminarCurso(Long id) {
        cursoRepository.deleteById(id);
    }
}
