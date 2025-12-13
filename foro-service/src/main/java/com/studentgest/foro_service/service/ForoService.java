package com.studentgest.foro_service.service;

import com.studentgest.foro_service.dto.*;

import java.util.List;

public interface ForoService {
    ForoResponse crearForo(ForoRequest request);
    ForoResponse agregarMensaje(Long foroId, MensajeRequest request);
    List<ForoResponse> obtenerTodosLosForos();
    ForoResponse obtenerForoPorId(Long id);
    ForoResponse actualizarForo(Long id, ForoRequest request);
    void eliminarForo(Long id);
    // Nuevo método
    List<ForoResponse> obtenerForosPorCurso(Long cursoId);
    List<MensajeResponse> obtenerMensajesPorForo(Long foroId);
}