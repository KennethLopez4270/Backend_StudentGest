package com.estudentgest.student_service.service;

import com.estudentgest.student_service.dto.UsuarioDTO;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioClient usuarioClient;

    public UsuarioService(UsuarioClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    public UsuarioDTO obtenerUsuario(Long idUsuario) {
        return usuarioClient.getUsuarioById(idUsuario);
    }
}
