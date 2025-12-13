package com.studentgest.user_service.service;

import com.studentgest.user_service.model.EstadoUsuario;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.User;
import com.studentgest.user_service.repository.RolRepository;
import com.studentgest.user_service.repository.UserRepository;
import com.studentgest.user_service.security.JwtUtil;
import com.studentgest.user_service.service.EmailVerificationService;
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
    private RolRepository rolRepository;

    @Autowired
    private PasswordHistoryService passwordHistoryService;

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityConfigService securityConfigService;

    @Autowired
    private EmailVerificationService emailVerificationService;

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
            logger.info("ID Rol: {}", user.getId_rol());

            // Validar campos requeridos
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("El email es requerido");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es requerida");
            }

            // Lógica para asignar ROL desde DB
            Integer idRolAsignar = null;
            Rol rolAsignar = null; // ✅ Capture the Rol entity

            if (user.getId_rol() != null) {
                // Si viene el ID, verificar que exista
                rolAsignar = rolRepository.findById(user.getId_rol())
                        .orElseThrow(
                                () -> new IllegalArgumentException("Rol no encontrado con ID: " + user.getId_rol()));
                idRolAsignar = user.getId_rol();
            } else if (user.getRol() != null && user.getRol().getNombre() != null) {
                // Si viene el objeto con nombre
                String nombreRol = user.getRol().getNombre();
                rolAsignar = rolRepository.findByNombre(nombreRol)
                        .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con nombre: " + nombreRol));
                idRolAsignar = rolAsignar.getIdRol();
            } else {
                throw new IllegalArgumentException("El rol es requerido (id_rol o objeto rol con nombre)");
            }

            // Validar que el email no exista
            String email = user.getEmail().toLowerCase().trim();
            if (repository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("El email ya está registrado");
            }

            // Validar política de contraseñas
            if (!passwordPolicyService.validatePassword(user.getPassword())) {
                throw new IllegalArgumentException("La contraseña no cumple con las políticas de seguridad: "
                        + passwordPolicyService.getPasswordRequirements());
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
            newUser.setId_rol(idRolAsignar); // Asignar el ID del rol
            newUser.setRol(rolAsignar); // ✅ Asignar la entidad Rol para que aparezca en la respuesta
            newUser.setEstado(EstadoUsuario.APROBADO); // ✅ APROBAR AUTOMÁTICAMENTE
            newUser.setActivo(true); // ✅ ACTIVAR AUTOMÁTICAMENTE
            newUser.setIntentosFallidos(0);
            newUser.setBloqueado(false);
            newUser.setRequiresPasswordChange(false);

            // ✅ NUEVO: Inicializar estado_gmail como "verificado" para permitir login
            // inmediato
            newUser.setEstadoGmail("verificado");

            // Establecer timestamps
            newUser.setCreado_en(new Timestamp(System.currentTimeMillis()));
            newUser.setUltimoCambioPassword(new Timestamp(System.currentTimeMillis()));

            // Establecer expiración de contraseña (90 días)
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(90);
            newUser.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));

            User savedUser = repository.save(newUser);
            logger.info("✅ Usuario creado exitosamente: {}", savedUser.getEmail());
            logger.info("🔄 INICIANDO ENVÍO DE EMAIL DE VERIFICACIÓN...");
            logger.info("📧 Destinatario: {}", savedUser.getEmail());
            logger.info("👤 Nombre: {}", savedUser.getNombre());

            // ✅ NUEVO: Enviar email de verificación
            try {
                boolean emailSent = emailVerificationService.sendVerificationEmail(savedUser);
                if (emailSent) {
                    logger.info("📧 Email de verificación enviado a: {}", savedUser.getEmail());
                } else {
                    logger.error("❌ Error al enviar email de verificación a: {}", savedUser.getEmail());
                }
            } catch (Exception e) {
                logger.error("❌ Error enviando email de verificación: {}", e.getMessage());
                // No lanzar excepción para no bloquear el registro
            }

            // ✅ NUEVO: Guardar la contraseña inicial en el historial
            passwordHistoryService.addToPasswordHistory(savedUser.getId_usuario(), hashedPassword);

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
        logger.info("📧 Estado Gmail: {}", user.getEstadoGmail());
        logger.info("🔓 Activo: {}", user.isActivo());
        logger.info("🔐 Contraseña en DB: {}", user.getPassword());
        logger.info("👤 Rol: {}", user.getRol());

        checkAndUpdatePasswordExpiration(user);

        // Verificar si está bloqueado
        if (user.isBloqueado()) {
            logger.warn("🚫 USUARIO BLOQUEADO: {}", email);
            auditLogService.logLoginAttempt(email, false, ipAddress);
            return Map.of("success", false, "message", "Cuenta bloqueada. Contacte al administrador.");
        }

        // ✅ NUEVO: Verificar que el usuario esté APROBADO
        if (!EstadoUsuario.APROBADO.equals(user.getEstado())) {
            logger.warn("⏳ USUARIO NO APROBADO: {} - Estado: {}", email, user.getEstado());
            auditLogService.logLoginAttempt(email, false, ipAddress);

            String message = "Cuenta pendiente de aprobación administrativa. Contacte al administrador.";
            if (EstadoUsuario.PENDIENTE.equals(user.getEstado())) {
                message = "Cuenta pendiente de aprobación administrativa. Contacte al administrador.";
            } else if (EstadoUsuario.RECHAZADO.equals(user.getEstado())) {
                message = "Cuenta rechazada. Contacte al administrador para más información.";
            }

            return Map.of("success", false, "message", message);
        }

        // ✅ NUEVO: Verificar que el email esté VERIFICADO
        if (!"verificado".equalsIgnoreCase(user.getEstadoGmail())) {
            logger.warn("📧 EMAIL NO VERIFICADO: {} - Estado Gmail: {}", email, user.getEstadoGmail());
            auditLogService.logLoginAttempt(email, false, ipAddress);

            String message = "Email no verificado. Por favor verifica tu email antes de iniciar sesión.";
            if ("pendiente".equalsIgnoreCase(user.getEstadoGmail())) {
                message = "Email pendiente de verificación. Revisa tu bandeja de entrada y haz clic en el enlace de verificación.";
            } else if ("expirado".equalsIgnoreCase(user.getEstadoGmail())) {
                message = "El enlace de verificación ha expirado. Solicita un nuevo enlace desde la página de login.";
            }

            return Map.of("success", false, "message", message);
        }

        // Verificar estado de activación
        if (!user.isActivo()) {
            logger.warn("⏳ USUARIO INACTIVO: {}", email);
            auditLogService.logLoginAttempt(email, false, ipAddress);
            return Map.of("success", false, "message", "Cuenta inactiva. Contacte al administrador.");
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
            // Obtener el nombre del rol desde la entidad Rol cargada (Lazy loading o ya
            // disponible)
            String nombreRol = user.getRol() != null ? user.getRol().getNombre() : "UNKNOWN";
            String token = jwtUtil.generateToken(user.getEmail(), nombreRol, user.getId_usuario());

            auditLogService.logLoginAttempt(email, true, ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("id", user.getId_usuario());
            response.put("nombre", user.getNombre());
            response.put("email", user.getEmail());
            response.put("rol", user.getRol());
            // Ensure id_rol is available for frontend functionality fetching
            response.put("id_rol", user.getId_rol());
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
                    user.isBloqueado() ? "Cuenta bloqueada por múltiples intentos fallidos"
                            : "Credenciales incorrectas");
        }
    }

    // ✅ NUEVO: Método para reenviar verificación de email
    public Map<String, Object> resendEmailVerification(String email) {
        try {
            Optional<User> userOptional = getUserByEmail(email);
            if (userOptional.isEmpty()) {
                return Map.of("success", false, "message", "Usuario no encontrado");
            }

            User user = userOptional.get();

            // Si ya está verificado, no hacer nada
            if ("verificado".equalsIgnoreCase(user.getEstadoGmail())) {
                return Map.of("success", true, "message", "El email ya está verificado");
            }

            // Reenviar email de verificación
            boolean emailSent = emailVerificationService.sendVerificationEmail(user);

            if (emailSent) {
                logger.info("📧 Email de verificación reenviado a: {}", email);
                return Map.of("success", true, "message", "Email de verificación reenviado exitosamente");
            } else {
                logger.error("❌ Error reenviando email de verificación a: {}", email);
                return Map.of("success", false, "message", "Error al reenviar email de verificación");
            }

        } catch (Exception e) {
            logger.error("❌ Error en resendEmailVerification: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "Error interno del servidor");
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
            String nuevaPasswordHash = passwordEncoder.encode(nuevaPassword);

            // Verificar que no sea la contraseña actual
            if (passwordEncoder.matches(nuevaPassword, user.getPassword())) {
                throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
            }

            // Verificar que no esté en el historial
            if (passwordHistoryService.isPasswordInHistory(userId, nuevaPassword)) {
                Map<String, Object> config = securityConfigService.loadSecurityConfig();
                int historySize = (Integer) config.get("passwordHistorySize");
                throw new IllegalArgumentException("No puede reutilizar las últimas " + historySize + " contraseñas");
            }

            // Guardar contraseña actual en historial
            passwordHistoryService.addToPasswordHistory(userId, user.getPassword());

            // Actualizar contraseña
            user.setPassword(nuevaPasswordHash);
            user.setBloqueado(false);
            user.setIntentosFallidos(0);
            user.setRequiresPasswordChange(false);
            user.setUltimoCambioPassword(new Timestamp(System.currentTimeMillis()));

            // Establecer expiración desde configuración
            Map<String, Object> config = securityConfigService.loadSecurityConfig();
            int expiryDays = (Integer) config.get("passwordExpiryDays");
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(expiryDays);
            user.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));

            repository.save(user);
            auditLogService.logPasswordChange(userId, ipAddress);
            return true;

        }).orElse(false);
    }

    public User updateUser(Integer id, User userDetails) {
        return repository.findById(id).map(user -> {
            // ... actualización de otros campos ...

            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                if (!passwordPolicyService.validatePassword(userDetails.getPassword())) {
                    throw new IllegalArgumentException("La contraseña no cumple con las políticas de seguridad");
                }

                String nuevaPasswordHash = passwordEncoder.encode(userDetails.getPassword());

                // ✅ NUEVO: Verificar que no esté en el historial
                if (passwordHistoryService.isPasswordInHistory(id, userDetails.getPassword())) {
                    throw new IllegalArgumentException("No puede reutilizar contraseñas anteriores");
                }

                // ✅ NUEVO: Guardar contraseña actual en historial
                passwordHistoryService.addToPasswordHistory(id, user.getPassword());

                user.setPassword(nuevaPasswordHash);
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

    public List<User> getUsuariosPorRol(String nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol).orElse(null);
        if (rol == null)
            return List.of();
        return repository.findByRol(rol);
    }

    public void aprobarUsuario(Integer id) {
        repository.findById(id).ifPresentOrElse(user -> {
            if (user.getEstado() == EstadoUsuario.APROBADO) {
                throw new IllegalStateException("El usuario ya está aprobado");
            }
            user.setEstado(EstadoUsuario.APROBADO);
            repository.save(user);
            logger.info("✅ Usuario aprobado exitosamente: {}", id);
        }, () -> {
            throw new RuntimeException("Usuario no encontrado");
        });
    }

    public void desaprobarUsuario(Integer id) {
        repository.findById(id).ifPresentOrElse(user -> {
            if (user.getEstado() == EstadoUsuario.RECHAZADO) {
                throw new IllegalStateException("El usuario ya está rechazado");
            }
            user.setEstado(EstadoUsuario.RECHAZADO);
            repository.save(user);
            logger.info("🚫 Usuario rechazado exitosamente: {}", id);
        }, () -> {
            throw new RuntimeException("Usuario no encontrado");
        });
    }

    public void activarUsuario(Integer id) {
        repository.findById(id).ifPresent(user -> {
            user.setActivo(true);
            user.setEstado(EstadoUsuario.APROBADO);
            repository.save(user);
        });
    }

    // ✅ NUEVO: Método para verificar y actualizar expiración de contraseña
    private void checkAndUpdatePasswordExpiration(User user) {
        try {
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            Timestamp fechaExpiracion = user.getFechaExpiracionPassword();

            logger.info("🔍 Verificando expiración de contraseña para: {}", user.getEmail());
            logger.info("📅 Fecha actual: {}", ahora);
            logger.info("📅 Fecha expiración: {}", fechaExpiracion);

            if (fechaExpiracion != null && fechaExpiracion.before(ahora)) {
                logger.warn("⏰ CONTRASEÑA EXPIRADA para: {}", user.getEmail());
                user.setRequiresPasswordChange(true);
                repository.save(user);
                logger.info("✅ requires_password_change actualizado a TRUE");
            } else if (fechaExpiracion != null) {
                long diasRestantes = (fechaExpiracion.getTime() - ahora.getTime()) / (1000 * 60 * 60 * 24);
                logger.info("✅ Contraseña vigente. Días restantes: {}", diasRestantes);
            }

        } catch (Exception e) {
            logger.error("❌ Error verificando expiración de contraseña: {}", e.getMessage());
        }
    }

    // En UserService.java - Corregir el método getVerificationStatus (estaba mal
    // ubicado)

    // ... código anterior ...

    // ✅ NUEVO: Método para verificar estado de verificación (FUERA del método
    // updatePasswordExpirationDate)
    public Map<String, Object> getVerificationStatus(String email) {
        try {
            Optional<User> userOptional = getUserByEmail(email);
            if (userOptional.isEmpty()) {
                return Map.of("success", false, "message", "Usuario no encontrado");
            }

            User user = userOptional.get();
            return Map.of(
                    "success", true,
                    "estado", user.getEstado().toString(),
                    "estadoGmail", user.getEstadoGmail(),
                    "fullyVerified", EstadoUsuario.APROBADO.equals(user.getEstado()) &&
                            "verificado".equalsIgnoreCase(user.getEstadoGmail()));

        } catch (Exception e) {
            logger.error("❌ Error obteniendo estado de verificación: {}", e.getMessage());
            return Map.of("success", false, "message", "Error interno del servidor");
        }
    }

    // El método updatePasswordExpirationDate debe terminar ANTES de
    // getVerificationStatus
    public void updatePasswordExpirationDate(Integer userId) {
        try {
            Optional<User> userOptional = repository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Obtener días de expiración desde la configuración de seguridad
                Map<String, Object> config = securityConfigService.loadSecurityConfig();
                int expiryDays = (Integer) config.get("passwordExpiryDays");

                // Calcular nueva fecha de expiración
                LocalDateTime expirationDate = LocalDateTime.now().plusDays(expiryDays);
                user.setFechaExpiracionPassword(Timestamp.valueOf(expirationDate));
                user.setRequiresPasswordChange(false); // Resetear el flag

                repository.save(user);

                logger.info("✅ Fecha de expiración actualizada para usuario {}: {} días hasta {}",
                        userId, expiryDays, expirationDate);
            }
        } catch (Exception e) {
            logger.error("❌ Error actualizando fecha de expiración: {}", e.getMessage());
        }
    }

}