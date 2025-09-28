package com.estudentgest.student_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Estudiante_Curso")
public class EstudianteCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ec;

    @Column(name = "id_estudiante", nullable = false)
    private Long idEstudiante;

    @Column(name = "id_curso", nullable = false)
    private Long idCurso;
}
