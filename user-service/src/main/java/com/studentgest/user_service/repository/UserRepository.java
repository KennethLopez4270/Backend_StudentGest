package com.studentgest.user_service.repository;

import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    List<User> findByActivoTrue();
    List<User> findByRol(Rol rol);
}
