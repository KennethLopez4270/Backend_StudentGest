package com.studentgest.user_service.service;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return repository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User createUser(User user) {
        // Establecer estado a PENDIENTE por defecto
        user.setEstado(EstadoUsuario.PENDIENTE);
        return repository.save(user);
    }

    public User updateUser(Integer id, User userDetails) {
        return repository.findById(id).map(user -> {
            user.setNombre(userDetails.getNombre());
            user.setApellido_paterno(userDetails.getApellido_paterno());
            user.setApellido_materno(userDetails.getApellido_materno());
            user.setEmail(userDetails.getEmail());
            user.setRol(userDetails.getRol());
            user.setFoto(userDetails.getFoto());
            if (userDetails.getPassword() != null) {
                user.setPassword(userDetails.getPassword()); // Hashea automáticamente en el setter
            }
            return repository.save(user);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return hashPassword(rawPassword).equals(hashedPassword);
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

    public List<User> getUsuariosActivos() {
        return repository.findByActivoTrue();
    }

    public void desactivarUsuario(Integer id) {
        repository.findById(id).ifPresent(user -> {
            user.setActivo(false);
            repository.save(user);
        });
    }

    public List<User> getUsuariosPorRol(Rol rol) {
        return repository.findByRol(rol);
    }

    public void activarUsuario(Integer id) {
        repository.findById(id).ifPresent(user -> {
            user.setActivo(true);
            user.setEstado(EstadoUsuario.APROBADO);
            repository.save(user);
        });
    }
}
