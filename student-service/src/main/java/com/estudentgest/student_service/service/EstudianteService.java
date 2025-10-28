// src/main/java/com/estudentgest/student_service/service/EstudianteService.java
package com.estudentgest.student_service.service;

import com.estudentgest.student_service.dto.EstudianteUsuarioDTO;
import com.estudentgest.student_service.dto.UsuarioDTO;
import com.estudentgest.student_service.model.Estudiante;
import com.estudentgest.student_service.repository.EstudianteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EstudianteService {

    private final UsuarioClient usuarioClient;
    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository, UsuarioClient usuarioClient) {
        this.estudianteRepository = estudianteRepository;
        this.usuarioClient = usuarioClient;
    }

    public List<Estudiante> obtenerTodosLosEstudiantes() {
        return estudianteRepository.findAll();
    }

    public List<EstudianteUsuarioDTO> getEstudiantesConUsuario() {
        List<Estudiante> estudiantes = estudianteRepository.findAll();
        List<EstudianteUsuarioDTO> resultado = new ArrayList<>();

        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getIdUsuario() != null) {
                UsuarioDTO usuario = usuarioClient.getUsuarioById(estudiante.getIdUsuario());
                if (usuario != null) {
                    EstudianteUsuarioDTO dto = new EstudianteUsuarioDTO();
                    dto.setId_estudiante(estudiante.getIdEstudiante());
                    dto.setId_usuario(usuario.getId_usuario());
                    dto.setNombre(usuario.getNombre());
                    dto.setApellido_paterno(usuario.getApellido_paterno());
                    dto.setApellido_materno(usuario.getApellido_materno());
                    dto.setEmail(usuario.getEmail());
                    dto.setPassword(usuario.getPassword());
                    dto.setCi(estudiante.getCi());
                    dto.setFecha_nacimiento(estudiante.getFechaNacimiento().toString());
                    dto.setId_rol(usuario.getId_rol());
                    dto.setRol(usuario.getRol());
                    dto.setEstado(usuario.getEstado());
                    dto.setFoto(usuario.getFoto());
                    dto.setActivo(usuario.getActivo());
                    dto.setCreado_en(usuario.getCreado_en()); // Nuevo

                    resultado.add(dto);
                }
            }
        }
        return resultado;
    }

    public EstudianteUsuarioDTO getEstudianteUsuarioByIdEstudiante(Long idEstudiante) {
        return estudianteRepository.findById(idEstudiante)
                .map(estudiante -> {
                    if (estudiante.getIdUsuario() == null) return null;
                    UsuarioDTO usuario = usuarioClient.getUsuarioById(estudiante.getIdUsuario());
                    if (usuario == null) return null;

                    EstudianteUsuarioDTO dto = new EstudianteUsuarioDTO();
                    dto.setId_estudiante(estudiante.getIdEstudiante());
                    dto.setId_usuario(usuario.getId_usuario());
                    dto.setNombre(usuario.getNombre());
                    dto.setApellido_paterno(usuario.getApellido_paterno());
                    dto.setApellido_materno(usuario.getApellido_materno());
                    dto.setEmail(usuario.getEmail());
                    dto.setPassword(usuario.getPassword());
                    dto.setCi(estudiante.getCi());
                    dto.setFecha_nacimiento(estudiante.getFechaNacimiento().toString());
                    dto.setId_rol(usuario.getId_rol());
                    dto.setRol(usuario.getRol());
                    dto.setEstado(usuario.getEstado());
                    dto.setFoto(usuario.getFoto());
                    dto.setActivo(usuario.getActivo());
                    dto.setCreado_en(usuario.getCreado_en());
                    return dto;
                })
                .orElse(null);
    }

    public EstudianteUsuarioDTO getEstudianteUsuarioByIdUsuario(Long idUsuario) {
        Estudiante estudiante = estudianteRepository.findByIdUsuario(idUsuario);
        return estudiante != null ? getEstudianteUsuarioByIdEstudiante(estudiante.getIdEstudiante()) : null;
    }
}