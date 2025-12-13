package com.studentgest.user_service.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "rol")
    private Set<RolesFuncionalidades> rolesFuncionalidades = new HashSet<>();

    // Getters y setters
    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<RolesFuncionalidades> getRolesFuncionalidades() {
        return rolesFuncionalidades;
    }

    public void setRolesFuncionalidades(Set<RolesFuncionalidades> rolesFuncionalidades) {
        this.rolesFuncionalidades = rolesFuncionalidades;
    }
}
