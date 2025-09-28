package com.estudentgest.student_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Curso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_curso;

    private int id_institucion = 1; // valor por defecto

    private String nombre;
    private String nivel;
    private String turno;
    private int gestion;
}
