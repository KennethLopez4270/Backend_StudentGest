package com.estudentgest.student_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CursoMateriaProfesorDTO {
    private Long idCmp; //aumente esto
    private String nombreCurso;
    private String nivel;
    private String turno;
    private int gestion;
    private String nombreMateria;
    private List<String> nombresEstudiantes; // ✅ este campo faltaba
}
