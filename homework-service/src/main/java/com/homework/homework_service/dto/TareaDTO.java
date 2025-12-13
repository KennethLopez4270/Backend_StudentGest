package com.homework.homework_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TareaDTO {
    private Integer idCmp;
    private String titulo;
    private String descripcion;
    private LocalDate fechaEntrega;
}
