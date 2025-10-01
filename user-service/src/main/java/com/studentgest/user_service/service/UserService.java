package com.studentgest.user_service.service;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.UserRepository;
import com.studentgest.user_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return repository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return repository.findByEmail(email.toLowerCase().trim());
    }

    public User createUser(User user) {
        try {
            logger.info("=== CREANDO USUARIO ===");
            logger.info("Email: {}", user.getEmail());
            logger.info("Nombre: {}", user.getNombre());
            logger.info("Rol: {}", user.getRol());
    
            // Validar campos requeridos
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("El email es requerido");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es requerida");
            }
            if (user.getRol() == null) {
                throw new IllegalArgumentException("El rol es requerido");
            }
    
            // Validar que el email no exista
            String email = user.getEmail().toLowerCase().trim();
            if (repository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("El email ya está registrado");
            }
    
            // Validar política de contraseñas
            if (!passwordPolicyService.validatePassword(user.getPassword())) {
                throw new IllegalArgumentException("La contraseña no cumple con las políticas de seguridad: " + passwordPolicyService.getPasswordRequirements());
            }
    
            // ✅ HASHEAR LA CONTRASEÑA CON BCRYPT
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            logger.info("🔐 Contraseña hasheada correctamente");
    
            // **SOLUCIÓN: Crear usuario SIN usar @Builder para evitar problemas**
            User newUser = new User();
            newUser.setNombre(user.getNombre());
            newUser.setApellido_paterno(user.getApellido_paterno());
            newUser.setApellido_materno(user.getApellido_materno());
            newUser.setEmail(email);
            newUser.setPassword(hashedPassword);
            newUser.setRol(user.getRol());
            newUser.setEstado(EstadoUsuario.APROBADO); // ✅ APROBAR AUTOMÁTICAMENTE
            newUser.setActivo(true); // ✅ ACTIVAR AUTOMÁTICAMENTE
            newUser.setIntentosFallidos(0);
            newUser.setBloqueado(false);
            newUser.setRequiresPasswordChange(false);
            
            // Establecer timestamps
            newUser.setCreado_en(new Timestamp(System.currentTimeMillis()));
            newUser.setUltimoCambioPassword(new Timestamp(System.currentTimeMillis()));
            
            // Establecer expiración de contraseña (90 días)
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(90);
            newUser.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));
    
            User savedUser = repository.save(newUser);
            logger.info("✅ Usuario creado exitosamente: {}", savedUser.getEmail());
            
            return savedUser;
    
        } catch (DataIntegrityViolationException e) {
            logger.error("❌ Error de integridad de datos: {}", e.getMessage());
            throw new IllegalArgumentException("Error de base de datos: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error de validación: {}", e.getMessage());
            throw e; // Re-lanzar para que el controller lo capture
        } catch (Exception e) {
            logger.error("💥 ERROR inesperado al crear usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno al crear usuario: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> login(String email, String password, String ipAddress) {
        logger.info("=== INICIANDO LOGIN ===");
        logger.info("Email recibido: {}", email);
        logger.info("IP Address: {}", ipAddress);
        
        Optional<User> userOptional = getUserByEmail(email);
        
        if (userOptional.isEmpty()) {
            logger.warn("❌ USUARIO NO ENCONTRADO: {}", email);
            auditLogService.logLoginAttempt(email, false, ipAddress);
            return Map.of("success", false, "message", "Credenciales incorrectas");
        }
        
        User user = userOptional.get();
        logger.info("✅ USUARIO ENCONTRADO: {}", user.getEmail());
        logger.info("📝 Estado: {}", user.getEstado());
        logger.info("🔓 Activo: {}", user.isActivo());
        logger.info("🔐 Contraseña en DB: {}", user.getPassword());
        logger.info("👤 Rol: {}", user.getRol());
        
        // Verificar si está bloqueado
        if (user.isBloqueado()) {
            logger.warn("🚫 USUARIO BLOQUEADO: {}", email);
            auditLogService.logLoginAttempt(email, false, ipAddress);
            return Map.of("success", false, "message", "Cuenta bloqueada. Contacte al administrador.");
        }
        
        // Verificar estado y activación
        if (!EstadoUsuario.APROBADO.equals(user.getEstado()) || !user.isActivo()) {
            logger.warn("⏳ USUARIO NO APROBADO/INACTIVO: {} - Estado: {} - Activo: {}", 
                       email, user.getEstado(), user.isActivo());
            auditLogService.logLoginAttempt(email, false, ipAddress);
            return Map.of("success", false, "message", "Cuenta no activa o pendiente de aprobación");
        }
        
        // VERIFICAR CONTRASEÑA
        logger.info("🔍 Verificando contraseña...");
        boolean passwordMatch = false;
        try {
            passwordMatch = passwordEncoder.matches(password, user.getPassword());
            logger.info("🔍 Resultado verificación contraseña: {}", passwordMatch);
        } catch (Exception e) {
            logger.error("❌ ERROR al verificar contraseña: {}", e.getMessage());
            auditLogService.logLoginAttempt(email, false, ipAddress);
            return Map.of("success", false, "message", "Error interno del servidor");
        }
        
        if (passwordMatch) {
            logger.info("🎉 LOGIN EXITOSO para: {}", email);
            // Resetear intentos fallidos en login exitoso
            user.setIntentosFallidos(0);
            repository.save(user);
            
            // Generar token JWT
            String token = jwtUtil.generateToken(user.getEmail(), user.getRol().name(), user.getId_usuario());
            
            auditLogService.logLoginAttempt(email, true, ipAddress);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("id", user.getId_usuario());
            response.put("nombre", user.getNombre());
            response.put("email", user.getEmail());
            response.put("rol", user.getRol());
            response.put("sessionTimeout", user.getSessionTimeout());
            response.put("requiresPasswordChange", user.isRequiresPasswordChange());
            
            logger.info("✅ Login completado exitosamente para: {}", email);
            return response;
        } else {
            logger.warn("❌ CONTRASEÑA INCORRECTA para: {}", email);
            // Incrementar intentos fallidos
            user.setIntentosFallidos(user.getIntentosFallidos() + 1);
            logger.info("📊 Intentos fallidos: {}", user.getIntentosFallidos());
            
            // Bloquear después de 3 intentos
            if (user.getIntentosFallidos() >= 3) {
                user.setBloqueado(true);
                logger.warn("🚫 CUENTA BLOQUEADA por múltiples intentos: {}", email);
                auditLogService.logAccountLocked(email, ipAddress);
            }
            
            repository.save(user);
            auditLogService.logLoginAttempt(email, false, ipAddress);
            
            return Map.of("success", false, "message", 
                user.isBloqueado() ? "Cuenta bloqueada por múltiples intentos fallidos" : "Credenciales incorrectas");
        }
    }
    
    public void desbloquearUsuario(Integer userId) {
        repository.findById(userId).ifPresent(user -> {
            user.setBloqueado(false);
            user.setIntentosFallidos(0);
            repository.save(user);
        });
    }
    
    public boolean forzarCambioPassword(Integer userId, String nuevaPassword, String ipAddress) {
        if (!passwordPolicyService.validatePassword(nuevaPassword)) {
            throw new IllegalArgumentException("La contraseña no cumple con las políticas de seguridad");
        }
        
        return repository.findById(userId).map(user -> {
            // Verificar que no sea la contraseña actual
            if (passwordEncoder.matches(nuevaPassword, user.getPassword())) {
                throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
            }
            
            // HASHEAR LA NUEVA CONTRASEÑA
            String hashedPassword = passwordEncoder.encode(nuevaPassword);
            user.setPassword(hashedPassword);
            user.setBloqueado(false);
            user.setIntentosFallidos(0);
            user.setRequiresPasswordChange(false);
            user.setUltimoCambioPassword(new Timestamp(System.currentTimeMillis()));
            
            // Establecer nueva expiración (90 días)
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(90);
            user.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));
            
            repository.save(user);
            auditLogService.logPasswordChange(userId, ipAddress);
            return true;
        }).orElse(false);
    }

    public User updateUser(Integer id, User userDetails) {
        return repository.findById(id).map(user -> {
            user.setNombre(userDetails.getNombre());
            user.setApellido_paterno(userDetails.getApellido_paterno());
            user.setApellido_materno(userDetails.getApellido_materno());
            user.setEmail(userDetails.getEmail().toLowerCase().trim());
            user.setRol(userDetails.getRol());
            user.setFoto(userDetails.getFoto());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                if (!passwordPolicyService.validatePassword(userDetails.getPassword())) {
                    throw new IllegalArgumentException("La contraseña no cumple con las políticas de seguridad");
                }
                // HASHEAR LA NUEVA CONTRASEÑA
                String hashedPassword = passwordEncoder.encode(userDetails.getPassword());
                user.setPassword(hashedPassword);
            }
            return repository.save(user);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
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