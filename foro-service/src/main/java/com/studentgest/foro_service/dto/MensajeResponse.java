package com.studentgest.foro_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MensajeResponse {
    private Long id;
    private Long foroId;
    private Long usuarioId;
    private String mensaje;
    private LocalDateTime fecha;
}