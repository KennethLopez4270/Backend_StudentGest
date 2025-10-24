package com.studentgest.user_service.repository;

import com.studentgest.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id_rol = :idRol")
    List<User> findByIdRol(Integer idRol);

    List<User> findByActivoTrue();
}