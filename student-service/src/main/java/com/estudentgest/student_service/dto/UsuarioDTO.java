package com.estudentgest.student_service.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id_usuario;
    private String nombre;
    private String apellido_paterno;
    private String apellido_materno;
    private String email;
    private String password;
    private String rol;
    private String foto;
    private Boolean activo;
    private String estado;
    private String creado_en;
}




