package com.studentgest.user_service.repository;

import com.studentgest.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar usuario por correo electrónico
    Optional<User> findByEmail(String email);

    // Buscar usuario por documento de identidad (CI o Pasaporte)
    Optional<User> findByDocumentoIdentidad(String documentoIdentidad);

    // Buscar usuario por su identificador único (userId)
    Optional<User> findByUserId(String userId);

    // Verificar si existe un usuario con el documento de identidad
    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    // Verificar si existe un usuario con el email
    boolean existsByEmail(String email);

    // Buscar todos los usuarios activos
    List<User> findByActivoTrue();

    // Buscar usuarios por el rol (usando el id del rol)
    List<User> findByIdRol(Long idRol);
}
