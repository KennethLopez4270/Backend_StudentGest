package com.homework.homework_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Entrega_Tarea")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntregaTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Long idEntrega;

    @Column(name = "id_tarea", nullable = false)
    private Long idTarea;

    @Column(name = "id_estudiante", nullable = false)
    private Long idEstudiante;

    @Column(length = 50)
    private String estado = "pendiente"; // Valor por defecto

    @Column(precision = 5, scale = 2)
    private BigDecimal calificacion;

    @Column(columnDefinition = "text")
    private String comentario;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
}
