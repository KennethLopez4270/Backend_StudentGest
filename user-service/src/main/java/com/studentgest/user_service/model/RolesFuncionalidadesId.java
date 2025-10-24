package com.studentgest.user_service.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class RolesFuncionalidadesId implements Serializable {
    private Integer rol;
    private Integer funcionalidad;

    // Constructores
    public RolesFuncionalidadesId() {}

    public RolesFuncionalidadesId(Integer rol, Integer funcionalidad) {
        this.rol = rol;
        this.funcionalidad = funcionalidad;
    }

    // Getters y setters
    public Integer getRol() { return rol; }
    public void setRol(Integer rol) { this.rol = rol; }
    public Integer getFuncionalidad() { return funcionalidad; }
    public void setFuncionalidad(Integer funcionalidad) { this.funcionalidad = funcionalidad; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolesFuncionalidadesId that = (RolesFuncionalidadesId) o;
        return rol.equals(that.rol) && funcionalidad.equals(that.funcionalidad);
    }

    @Override
    public int hashCode() {
        return 31 * rol.hashCode() + funcionalidad.hashCode();
    }
}