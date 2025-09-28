package com.estudentgest.student_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CursoMateriaEstudiantesDTO {
    private String nombreCurso;
    private String nivel;
    private String turno;
    private Integer gestion;
    private String nombreMateria;
    private List<Long> estudiantes; // Aquí cambiamos a IDs
}
