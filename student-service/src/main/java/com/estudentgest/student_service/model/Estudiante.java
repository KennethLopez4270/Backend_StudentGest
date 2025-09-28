package com.estudentgest.student_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "Estudiante")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Long idEstudiante;

    @Column(name = "id_institucion", nullable = false)
    private Integer idInstitucion;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "ci", nullable = false, length = 20)
    private String ci;

    @Column(name = "creado_en")
    private java.time.LocalDateTime creadoEn;
}
