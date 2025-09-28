package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificacionClient notificacionClient;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Integer id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Validación: estado debe ser APROBADO y activo debe ser true
            if (!EstadoUsuario.APROBADO.equals(user.getEstado()) || !user.isActivo()) {
                return Map.of("message", "Credenciales incorrectas");
            }

            if (userService.verifyPassword(password, user.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId_usuario());
                response.put("nombre", user.getNombre());
                response.put("apellido_paterno", user.getApellido_paterno());
                response.put("apellido_materno", user.getApellido_materno());
                response.put("email", user.getEmail());
                response.put("rol", user.getRol());
                response.put("foto", user.getFoto());
                return response;
            }
        }
        return Map.of("message", "Credenciales incorrectas");
    }

    @GetMapping("/me")
    public Map<String, String> getRolUsuario(@RequestParam Integer id) {
        return userService.getUserById(id)
                .map(user -> Map.of("rol", user.getRol().toString()))
                .orElse(Map.of("message", "Usuario no encontrado"));
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String newPassword = "ABCabc1234!";

            user.setPassword(newPassword); // Se hashea automáticamente
            userService.createUser(user); // Save actualizado

            // Aquí llamarías al microservicio de notificaciones
            notificacionClient.enviarNotificacionResetPassword(email, user.getNombre(), newPassword);

            return Map.of("message", "Se ha enviado un correo con la nueva contraseña.");
        } else {
            return Map.of("message", "Usuario no encontrado con ese correo.");
        }
    }

    @GetMapping("/activos")
    public List<User> getUsuariosActivos() {
        return userService.getUsuariosActivos();
    }

    @PutMapping("/desactivar/{id}")
    public Map<String, String> desactivarUsuario(@PathVariable Integer id) {
        userService.desactivarUsuario(id);
        return Map.of("message", "Usuario desactivado correctamente.");
    }

    @GetMapping("/rol/{rol}")
    public List<User> getUsuariosPorRol(@PathVariable Rol rol) {
        return userService.getUsuariosPorRol(rol);
    }

}
