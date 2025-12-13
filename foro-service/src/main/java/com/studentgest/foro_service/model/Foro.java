package com.studentgest.foro_service.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "foro")
public class Foro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_curso", nullable = false)
    private Long cursoId;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @OneToMany(mappedBy = "foro", cascade = CascadeType.ALL)
    private List<MensajeForo> mensajes;
    
}