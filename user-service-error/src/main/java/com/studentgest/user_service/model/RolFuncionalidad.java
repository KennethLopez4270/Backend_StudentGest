package com.studentgest.user_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles_funcionalidades")
public class RolFuncionalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_roles_funcionalidades")
    private Integer idRolesFuncionalidades;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "id_funcionalidad", nullable = false)
    private Funcionalidad funcionalidad;

    // Getters y Setters
    public Integer getIdRolesFuncionalidades() { return idRolesFuncionalidades; }
    public void setIdRolesFuncionalidades(Integer idRolesFuncionalidades) { this.idRolesFuncionalidades = idRolesFuncionalidades; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Funcionalidad getFuncionalidad() { return funcionalidad; }
    public void setFuncionalidad(Funcionalidad funcionalidad) { this.funcionalidad = funcionalidad; }
}
