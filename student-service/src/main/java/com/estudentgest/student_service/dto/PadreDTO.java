// src/main/java/com/estudentgest/student_service/dto/PadreDTO.java
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
    private Integer id_rol;        // Nuevo
    private String rol;
    private Boolean activo;
    private String estado;
    private String creado_en;      // Nuevo
}