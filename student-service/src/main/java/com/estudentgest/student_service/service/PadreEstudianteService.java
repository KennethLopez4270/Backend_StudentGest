package com.estudentgest.student_service.service;

import com.estudentgest.student_service.dto.EstudianteUsuarioDTO;
import com.estudentgest.student_service.dto.PadreDTO;
import com.estudentgest.student_service.dto.UsuarioDTO;
import com.estudentgest.student_service.model.Estudiante;
import com.estudentgest.student_service.model.PadreEstudiante;
import com.estudentgest.student_service.repository.EstudianteRepository;
import com.estudentgest.student_service.repository.PadreEstudianteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PadreEstudianteService {

    private final UsuarioClient usuarioClient;
    private final EstudianteRepository estudianteRepository;
    private final PadreEstudianteRepository padreEstudianteRepository;

    public PadreEstudianteService(UsuarioClient usuarioClient, EstudianteRepository estudianteRepository, PadreEstudianteRepository padreEstudianteRepository) {
        this.usuarioClient = usuarioClient;
        this.estudianteRepository = estudianteRepository;
        this.padreEstudianteRepository = padreEstudianteRepository;
    }

    // Obtener estudiantes por id del padre
    public List<EstudianteUsuarioDTO> obtenerEstudiantesPorPadre(Long idPadre) {
        UsuarioDTO usuario = usuarioClient.getUsuarioById(idPadre);
        if (usuario == null || !usuario.getRol().equalsIgnoreCase("padre")) {
            throw new IllegalArgumentException("Este usuario no es padre");
        }

        List<PadreEstudiante> relaciones = padreEstudianteRepository.findByIdPadre(idPadre);
        List<EstudianteUsuarioDTO> hijos = new ArrayList<>();

        for (PadreEstudiante relacion : relaciones) {
            Estudiante estudiante = estudianteRepository.findById(relacion.getIdEstudiante()).orElse(null);
            if (estudiante != null && estudiante.getIdUsuario() != null) {
                UsuarioDTO hijoUsuario = usuarioClient.getUsuarioById(estudiante.getIdUsuario());

                if (hijoUsuario != null) {
                    EstudianteUsuarioDTO dto = new EstudianteUsuarioDTO();
                    dto.setId_estudiante(estudiante.getIdEstudiante());
                    dto.setId_usuario(hijoUsuario.getId_usuario());
                    dto.setNombre(hijoUsuario.getNombre());
                    dto.setApellido_paterno(hijoUsuario.getApellido_paterno());
                    dto.setEmail(hijoUsuario.getEmail());
                    dto.setPassword(hijoUsuario.getPassword());
                    dto.setRol(hijoUsuario.getRol());
                    dto.setEstado(hijoUsuario.getEstado());
                    dto.setFoto(hijoUsuario.getFoto());
                    dto.setActivo(hijoUsuario.getActivo());

                    hijos.add(dto);
                }
            }
        }

        return hijos;
    }

    // Obtener padres por id del estudiante
    public List<PadreDTO> obtenerPadresPorEstudiante(Long idEstudiante) {
        List<PadreEstudiante> relaciones = padreEstudianteRepository.findByIdEstudiante(idEstudiante);
        List<PadreDTO> padres = new ArrayList<>();

        for (PadreEstudiante relacion : relaciones) {
            UsuarioDTO padre = usuarioClient.getUsuarioById(relacion.getIdPadre());
            if (padre != null && padre.getRol().equalsIgnoreCase("padre")) {
                PadreDTO dto = new PadreDTO();
                dto.setId_usuario(padre.getId_usuario());
                dto.setNombre(padre.getNombre());
                dto.setApellido_paterno(padre.getApellido_paterno());
                dto.setApellido_materno(padre.getApellido_materno());
                dto.setEmail(padre.getEmail());
                dto.setFoto(padre.getFoto());
                dto.setRol(padre.getRol());
                dto.setActivo(padre.getActivo());
                dto.setEstado(padre.getEstado());

                padres.add(dto);
            }
        }

        return padres;
    }

    public PadreEstudiante conectarPadreConEstudiante(PadreEstudiante padreEstudiante) {
        // Opcionalmente podrías validar que no se repita
        if (padreEstudianteRepository.existsByIdPadreAndIdEstudiante(
                padreEstudiante.getIdPadre(), padreEstudiante.getIdEstudiante())) {
            throw new IllegalArgumentException("Esta relación ya existe");
        }

        return padreEstudianteRepository.save(padreEstudiante);
    }

}
