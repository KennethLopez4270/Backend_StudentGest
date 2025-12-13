package com.estudentgest.student_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoDTO {
    private Long id_curso;
    private String nombre;
    private String nivel;
    private String turno;
    private int gestion;
}
