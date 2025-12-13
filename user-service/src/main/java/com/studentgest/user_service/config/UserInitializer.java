package com.studentgest.user_service.config;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.RolRepository;
import com.studentgest.user_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.util.Optional;

@Configuration
public class UserInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "admin.osi@ucb.edu.bo";
            if (userRepository.findByEmail(email).isEmpty()) {
                System.out.println("🚀 Initializing default OSI Admin user...");

                Optional<Rol> osiRol = rolRepository.findByNombre("OSI");
                if (osiRol.isPresent()) {
                    Rol rol = osiRol.get();

                    User admin = User.builder()
                            .nombre("Administrador")
                            .apellido_paterno("OSI")
                            .apellido_materno("System")
                            .email(email)
                            .password(passwordEncoder.encode("admin123")) // Default password
                            .id_rol(rol.getIdRol())
                            // .rol(rol) // Managed by id_rol + JoinColumn
                            .estado(EstadoUsuario.APROBADO)
                            .estadoGmail("verificado")
                            .activo(true)
                            .bloqueado(false)
                            .intentosFallidos(0)
                            .creado_en(new Timestamp(System.currentTimeMillis()))
                            .build();

                    userRepository.save(admin);
                    System.out.println("✅ Default OSI Admin user created: " + email + " / admin123");
                } else {
                    System.err.println(
                            "⚠️ Role 'OSI' not found! Skipping admin user creation. Ensure RolService initializes roles first.");
                }
            } else {
                System.out.println("ℹ️ OSI Admin user already exists.");
            }
        };
    }
}
