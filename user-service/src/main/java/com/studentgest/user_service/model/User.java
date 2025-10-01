package com.studentgest.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id_usuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido_paterno", length = 100)
    private String apellido_paterno;

    @Column(name = "apellido_materno", length = 100)
    private String apellido_materno;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Convert(converter = RolConverter.class)
    @Column(name = "rol", nullable = false, length = 50)
    private Rol rol;

    @Convert(converter = EstadoUsuarioConverter.class)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoUsuario estado = EstadoUsuario.PENDIENTE;

    @Column(name = "foto", columnDefinition = "TEXT")
    private String foto;

    @Column(name = "creado_en")
    @Builder.Default
    private Timestamp creado_en = new Timestamp(System.currentTimeMillis());

    @Column(name = "activo")
    @Builder.Default
    private boolean activo = true;

    // NUEVOS CAMPOS DE SEGURIDAD
    @Column(name = "intentos_fallidos")
    @Builder.Default
    private int intentosFallidos = 0;

    @Column(name = "bloqueado")
    @Builder.Default
    private boolean bloqueado = false;

    @Column(name = "fecha_expiracion_password")
    private Timestamp fechaExpiracionPassword;

    @Column(name = "ultimo_cambio_password")
    private Timestamp ultimoCambioPassword;

    @Transient
    @Builder.Default
    private List<String> historialPasswords = new ArrayList<>();

    @Column(name = "session_timeout")
    @Builder.Default
    private Integer sessionTimeout = 150;

    @Column(name = "requires_password_change")
    @Builder.Default
    private boolean requiresPasswordChange = false;

    @PrePersist
    protected void onCreate() {
        if (creado_en == null) {
            creado_en = new Timestamp(System.currentTimeMillis());
        }
        if (estado == null) {
            estado = EstadoUsuario.PENDIENTE;
        }
        if (ultimoCambioPassword == null) {
            ultimoCambioPassword = new Timestamp(System.currentTimeMillis());
        }
        if (fechaExpiracionPassword == null) {
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(90);
            fechaExpiracionPassword = Timestamp.valueOf(expirationDate);
        }
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido_paterno() {
        return apellido_paterno;
    }

    public void setApellido_paterno(String apellido_paterno) {
        this.apellido_paterno = apellido_paterno;
    }

    public String getApellido_materno() {
        return apellido_materno;
    }

    public void setApellido_materno(String apellido_materno) {
        this.apellido_materno = apellido_materno;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Timestamp getCreado_en() {
        return creado_en;
    }

    public void setCreado_en(Timestamp creado_en) {
        this.creado_en = creado_en;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public Timestamp getFechaExpiracionPassword() {
        return fechaExpiracionPassword;
    }

    public void setFechaExpiracionPassword(Timestamp fechaExpiracionPassword) {
        this.fechaExpiracionPassword = fechaExpiracionPassword;
    }

    public Timestamp getUltimoCambioPassword() {
        return ultimoCambioPassword;
    }

    public void setUltimoCambioPassword(Timestamp ultimoCambioPassword) {
        this.ultimoCambioPassword = ultimoCambioPassword;
    }

    public List<String> getHistorialPasswords() {
        return historialPasswords;
    }

    public void setHistorialPasswords(List<String> historialPasswords) {
        this.historialPasswords = historialPasswords;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public boolean isRequiresPasswordChange() {
        return requiresPasswordChange;
    }

    public void setRequiresPasswordChange(boolean requiresPasswordChange) {
        this.requiresPasswordChange = requiresPasswordChange;
    }

}