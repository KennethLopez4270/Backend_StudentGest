package com.studentgest.foro_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForoRequest {
    @NotNull(message = "El ID de curso es requerido")
    private Long cursoId;

    @NotBlank(message = "El título no puede estar vacío")
    private String titulo;

    @NotNull(message = "El ID del creador es requerido")
    private Long creadoPor;
}