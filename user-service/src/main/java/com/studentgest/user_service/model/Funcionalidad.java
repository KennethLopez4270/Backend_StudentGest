package com.studentgest.user_service.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "funcionalidades")
public class Funcionalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idFuncionalidad;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String direccion;

    @Column(name = "codigo")
    private String codigo;

    @OneToMany(mappedBy = "funcionalidad")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<RolesFuncionalidades> rolesFuncionalidades = new HashSet<>();

    // Getters y setters
    public Integer getIdFuncionalidad() {
        return idFuncionalidad;
    }

    public void setIdFuncionalidad(Integer idFuncionalidad) {
        this.idFuncionalidad = idFuncionalidad;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Set<RolesFuncionalidades> getRolesFuncionalidades() {
        return rolesFuncionalidades;
    }

    public void setRolesFuncionalidades(Set<RolesFuncionalidades> rolesFuncionalidades) {
        this.rolesFuncionalidades = rolesFuncionalidades;
    }
}
