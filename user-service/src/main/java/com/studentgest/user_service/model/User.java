package com.studentgest.user_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_usuario;

    private String nombre;
    private String apellido_paterno;
    private String apellido_materno;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Convert(converter = RolConverter.class)
    private Rol rol;

    @Convert(converter = EstadoUsuarioConverter.class)
    private EstadoUsuario estado;

    private String foto;

    private Timestamp creado_en = new Timestamp(System.currentTimeMillis());

    private boolean activo;

    public void setPassword(String password) {
        this.password = hashPassword(password);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }
}
