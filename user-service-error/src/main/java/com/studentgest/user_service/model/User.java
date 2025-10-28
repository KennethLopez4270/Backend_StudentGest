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

    // 🔹 NUEVO CAMPO (en lugar del Enum Rol)
    @Column(name = "id_rol")
    private Integer idRol;

    // 🔹 NUEVOS CAMPOS DE IDENTIFICACIÓN
    @Column(name = "documento_identidad", nullable = false, unique = true, length = 20)
    private String documentoIdentidad;

    @Column(name = "user_id", nullable = false, unique = true, length = 20)
    private String userId;

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

    // 🔹 MÉTODO ACTUALIZADO
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

        // 🔸 Validar documento_identidad (mínimo 10 caracteres)
        if (documentoIdentidad == null || documentoIdentidad.length() < 10) {
            throw new IllegalArgumentException("El documento de identidad debe tener al menos 10 caracteres.");
        }

        // 🔸 Generar automáticamente el user_id
        if (userId == null && documentoIdentidad != null && apellido_paterno != null && nombre != null) {
            userId = documentoIdentidad
                    + apellido_paterno.toUpperCase().charAt(0)
                    + nombre.toUpperCase().charAt(0);
        }
    }
}
