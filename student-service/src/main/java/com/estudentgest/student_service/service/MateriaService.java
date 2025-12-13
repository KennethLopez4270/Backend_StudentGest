package com.estudentgest.student_service.service;

import com.estudentgest.student_service.dto.MateriaDTO;
import com.estudentgest.student_service.model.Materia;
import com.estudentgest.student_service.repository.MateriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;

    public MateriaService(MateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    public List<Materia> getAllMaterias() {
        return materiaRepository.findAll();
    }

    public Optional<Materia> getMateriaById(Long id) {
        return materiaRepository.findById(id);
    }

    public Materia createMateria(MateriaDTO dto) {
        Materia materia = new Materia();
        materia.setNombre(dto.getNombre());
        return materiaRepository.save(materia);
    }

    public Optional<Materia> updateMateria(Long id, MateriaDTO dto) {
        Optional<Materia> materiaOptional = materiaRepository.findById(id);
        if (materiaOptional.isPresent()) {
            Materia materia = materiaOptional.get();
            materia.setNombre(dto.getNombre());
            return Optional.of(materiaRepository.save(materia));
        }
        return Optional.empty();
    }

    public void deleteMateria(Long id) {
        materiaRepository.deleteById(id);
    }
}
