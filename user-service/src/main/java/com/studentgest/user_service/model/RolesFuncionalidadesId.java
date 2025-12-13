package com.studentgest.user_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class RolesFuncionalidadesId implements Serializable {
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "id_funcionalidad")
    private Integer idFuncionalidad;

    // Constructores
    public RolesFuncionalidadesId() {
    }

    public RolesFuncionalidadesId(Integer idRol, Integer idFuncionalidad) {
        this.idRol = idRol;
        this.idFuncionalidad = idFuncionalidad;
    }

    // Getters y setters
    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public Integer getIdFuncionalidad() {
        return idFuncionalidad;
    }

    public void setIdFuncionalidad(Integer idFuncionalidad) {
        this.idFuncionalidad = idFuncionalidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RolesFuncionalidadesId that = (RolesFuncionalidadesId) o;
        return idRol.equals(that.idRol) && idFuncionalidad.equals(that.idFuncionalidad);
    }

    @Override
    public int hashCode() {
        return 31 * idRol.hashCode() + idFuncionalidad.hashCode();
    }
}
