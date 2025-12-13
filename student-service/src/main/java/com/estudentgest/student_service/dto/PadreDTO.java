package com.estudentgest.student_service.dto;

import lombok.Data;

@Data
public class PadreDTO {
    private Long id_usuario;
    private String nombre;
    private String apellido_paterno;
    private String apellido_materno;
    private String email;
    private String foto;
    private String rol;
    private Boolean activo;
    private String estado;
}

