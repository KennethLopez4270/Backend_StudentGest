package com.studentgest.user_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "funcionalidades")
public class Funcionalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcionalidad")
    private Integer idFuncionalidad;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // Getters y Setters
    public Integer getIdFuncionalidad() { return idFuncionalidad; }
    public void setIdFuncionalidad(Integer idFuncionalidad) { this.idFuncionalidad = idFuncionalidad; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
