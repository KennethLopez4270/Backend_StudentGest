package com.homework.homework_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class CursoMateriaEstudiantesDTO {
    private String nombreCurso;
    private String nivel;
    private String turno;
    private Integer gestion;
    private String nombreMateria;
    private List<Long> estudiantes;
}
