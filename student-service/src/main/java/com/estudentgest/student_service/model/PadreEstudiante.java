package com.estudentgest.student_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Padre_Estudiante", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_padre", "id_estudiante"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PadreEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relacion")
    private Long idRelacion;

    @Column(name = "id_padre", nullable = false)
    private Long idPadre;

    @Column(name = "id_estudiante", nullable = false)
    private Long idEstudiante;
}
