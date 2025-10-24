package com.studentgest.user_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles_funcionalidades")
public class RolesFuncionalidades {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionalidad")
    private Funcionalidad funcionalidad;

    @Column(name = "descripcion")
    private String descripcion;

    // Getters y setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Funcionalidad getFuncionalidad() { return funcionalidad; }
    public void setFuncionalidad(Funcionalidad funcionalidad) { this.funcionalidad = funcionalidad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}