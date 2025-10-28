package com.studentgest.user_service.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id_usuario;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "apellido_paterno")
    private String apellido_paterno;

    @Column(name = "apellido_materno")
    private String apellido_materno;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "foto")
    private String foto;

    @Column(name = "estado")
    @Convert(converter = EstadoUsuarioConverter.class)
    private EstadoUsuario estado;

    @Column(name = "activo")
    private boolean activo = true;

    @Column(name = "creado_en")
    private LocalDateTime creado_en;

    @Column(name = "id_rol")
    private Integer id_rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol", insertable = false, updatable = false)
    @JsonIgnore
    private Rol rol;

    // Getters y setters
    public Integer getId_usuario() { return id_usuario; }
    public void setId_usuario(Integer id_usuario) { this.id_usuario = id_usuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido_paterno() { return apellido_paterno; }
    public void setApellido_paterno(String apellido_paterno) { this.apellido_paterno = apellido_paterno; }

    public String getApellido_materno() { return apellido_materno; }
    public void setApellido_materno(String apellido_materno) { this.apellido_materno = apellido_materno; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getCreado_en() { return creado_en; }
    public void setCreado_en(LocalDateTime creado_en) { this.creado_en = creado_en; }

    public Integer getId_rol() { return id_rol; }
    public void setId_rol(Integer id_rol) { this.id_rol = id_rol; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}