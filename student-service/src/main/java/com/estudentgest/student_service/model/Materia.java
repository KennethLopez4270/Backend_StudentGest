package com.estudentgest.student_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Materia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_materia;

    private String nombre;
}
