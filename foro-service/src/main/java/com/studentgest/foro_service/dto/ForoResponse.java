package com.studentgest.foro_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ForoResponse {
    private Long id;
    private Long cursoId;
    private String titulo;
    private Long creadoPor;
    private LocalDateTime creadoEn;
    // Elimina temporalmente la lista de mensajes para pruebas
}