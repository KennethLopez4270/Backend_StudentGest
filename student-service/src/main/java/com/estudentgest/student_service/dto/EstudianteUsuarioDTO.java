package com.estudentgest.student_service.dto;

import lombok.Data;

@Data
public class EstudianteUsuarioDTO {
    private Long id_estudiante;
    private Long id_usuario;
    private String nombre;
    private String apellido_paterno;
    private String apellido_materno;
    private String email;
    private String password;
    private String ci;
    private String fecha_nacimiento;
    private String rol;
    private String estado;
    private String foto;
    private Boolean activo;
}
