package com.studentgest.user_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles_funcionalidades")
public class RolesFuncionalidades {

    @EmbeddedId
    private RolesFuncionalidadesId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol", insertable = false, updatable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionalidad", referencedColumnName = "idFuncionalidad", insertable = false, updatable = false)
    private Funcionalidad funcionalidad;

    // Getters y setters
    public RolesFuncionalidadesId getId() { return id; }
    public void setId(RolesFuncionalidadesId id) { this.id = id; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Funcionalidad getFuncionalidad() { return funcionalidad; }
    public void setFuncionalidad(Funcionalidad funcionalidad) { this.funcionalidad = funcionalidad; }
}