package com.studentgest.user_service.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

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

    // Función toString() que devuelve el nombre del rol en minúsculas
    @Override
    public String toString() {
        return this.nombre != null ? this.nombre.toLowerCase() : null;
    }
}


/* 
/// Molli
package com.studentgest.user_service.model;

public enum Rol {
    PROFESOR,
    PERSONAL,
    PADRE,
    ESTUDIANTE,
    DIRECTOR;

    @Override
    public String toString() {
        return name().toLowerCase(); // Esto devuelve "profesor", "personal", etc.
    }
}
*/
/*
/// Kenneth


package com.studentgest.user_service.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @OneToMany(mappedBy = "rol")
    private Set<RolesFuncionalidades> rolesFuncionalidades = new HashSet<>();

    // Getters y setters
    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Set<RolesFuncionalidades> getRolesFuncionalidades() { return rolesFuncionalidades; }
    public void setRolesFuncionalidades(Set<RolesFuncionalidades> rolesFuncionalidades) { this.rolesFuncionalidades = rolesFuncionalidades; }
}
*/