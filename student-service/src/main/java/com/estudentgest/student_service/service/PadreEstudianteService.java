// src/main/java/com/estudentgest/student_service/service/PadreEstudianteService.java
package com.estudentgest.student_service.service;

import com.estudentgest.student_service.dto.EstudianteUsuarioDTO;
import com.estudentgest.student_service.dto.PadreDTO;
import com.estudentgest.student_service.dto.UsuarioDTO;  // ← IMPORTA ESTO
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

    public PadreEstudianteService(UsuarioClient usuarioClient,
                                  EstudianteRepository estudianteRepository,
                                  PadreEstudianteRepository padreEstudianteRepository) {
        this.usuarioClient = usuarioClient;
        this.estudianteRepository = estudianteRepository;
        this.padreEstudianteRepository = padreEstudianteRepository;
    }

    public List<EstudianteUsuarioDTO> obtenerEstudiantesPorPadre(Long idPadre) {
        UsuarioDTO padre = usuarioClient.getUsuarioById(idPadre);
        if (padre == null || !"Padre".equalsIgnoreCase(padre.getRol())) {
            throw new IllegalArgumentException("Este usuario no es padre");
        }

        List<PadreEstudiante> relaciones = padreEstudianteRepository.findByIdPadre(idPadre);
        List<EstudianteUsuarioDTO> hijos = new ArrayList<>();

        for (PadreEstudiante relacion : relaciones) {
            Estudiante estudiante = estudianteRepository.findById(relacion.getIdEstudiante()).orElse(null);
            if (estudiante != null && estudiante.getIdUsuario() != null) {
                UsuarioDTO isHijoUsuario = usuarioClient.getUsuarioById(estudiante.getIdUsuario()); // ← CORREGIDO
                if (isHijoUsuario != null) {
                    EstudianteUsuarioDTO dto = new EstudianteUsuarioDTO();
                    dto.setId_estudiante(estudiante.getIdEstudiante());
                    dto.setId_usuario(isHijoUsuario.getId_usuario());
                    dto.setNombre(isHijoUsuario.getNombre());
                    dto.setApellido_paterno(isHijoUsuario.getApellido_paterno());
                    dto.setApellido_materno(isHijoUsuario.getApellido_materno());
                    dto.setEmail(isHijoUsuario.getEmail());
                    dto.setPassword(isHijoUsuario.getPassword());
                    dto.setCi(estudiante.getCi());
                    dto.setFecha_nacimiento(estudiante.getFechaNacimiento().toString());
                    dto.setId_rol(isHijoUsuario.getId_rol());
                    dto.setRol(isHijoUsuario.getRol());
                    dto.setEstado(isHijoUsuario.getEstado());
                    dto.setFoto(isHijoUsuario.getFoto());
                    dto.setActivo(isHijoUsuario.getActivo());
                    dto.setCreado_en(isHijoUsuario.getCreado_en());

                    hijos.add(dto);
                }
            }
        }
        return hijos;
    }

    public List<PadreDTO> obtenerPadresPorEstudiante(Long idEstudiante) {
        List<PadreEstudiante> relaciones = padreEstudianteRepository.findByIdEstudiante(idEstudiante);
        List<PadreDTO> padres = new ArrayList<>();

        for (PadreEstudiante relacion : relaciones) {
            UsuarioDTO padre = usuarioClient.getUsuarioById(relacion.getIdPadre()); // ← CORREGIDO
            if (padre != null && "Padre".equalsIgnoreCase(padre.getRol())) {
                PadreDTO dto = new PadreDTO();
                dto.setId_usuario(padre.getId_usuario());
                dto.setNombre(padre.getNombre());
                dto.setApellido_paterno(padre.getApellido_paterno());
                dto.setApellido_materno(padre.getApellido_materno());
                dto.setEmail(padre.getEmail());
                dto.setFoto(padre.getFoto());
                dto.setId_rol(padre.getId_rol());
                dto.setRol(padre.getRol());
                dto.setActivo(padre.getActivo());
                dto.setEstado(padre.getEstado());
                dto.setCreado_en(padre.getCreado_en());

                padres.add(dto);
            }
        }
        return padres;
    }

    public PadreEstudiante conectarPadreConEstudiante(PadreEstudiante padreEstudiante) {
        if (padreEstudianteRepository.existsByIdPadreAndIdEstudiante(
                padreEstudiante.getIdPadre(), padreEstudiante.getIdEstudiante())) {
            throw new IllegalArgumentException("Esta relación ya existe");
        }
        return padreEstudianteRepository.save(padreEstudiante);
    }
}