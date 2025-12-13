package com.estudentgest.student_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Curso_Materia_Profesor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoMateriaProfesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cmp")
    private Long idCmp;

    @Column(name = "id_curso")
    private Long idCurso;

    @Column(name = "id_materia")
    private Long idMateria;

    @Column(name = "id_profesor")
    private Long idProfesor;
}
