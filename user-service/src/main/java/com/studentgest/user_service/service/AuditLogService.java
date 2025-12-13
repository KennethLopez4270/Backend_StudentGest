package com.studentgest.user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * Servicio de auditoría para registrar eventos de seguridad de la aplicación.
 * Implementa logs de aplicación y logs de usuario para cumplir con requisitos
 * de seguridad.
 * 
 * Los logs se guardan en:
 * - Console: Durante desarrollo
 * - logs/security-audit.log: Archivo dedicado a eventos de seguridad
 */
@Service
public class AuditLogService {

    // Logger principal para eventos de seguridad
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_AUDIT");

    // Logger de aplicación para eventos generales
    private static final Logger appLogger = LoggerFactory.getLogger(AuditLogService.class);

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // =============================================
    // MÉTODOS BASE DE LOGGING
    // =============================================

    /**
     * Registra un evento de seguridad genérico
     */
    public void logSecurityEvent(String eventType, String description, Integer userId, String ipAddress) {
        String logMessage = String.format(
                "SECURITY_EVENT: type=%s, userId=%s, ip=%s, time=%s, description=%s",
                eventType,
                userId != null ? userId.toString() : "N/A",
                ipAddress != null ? ipAddress : "N/A",
                new Timestamp(System.currentTimeMillis()),
                description);

        securityLogger.info(logMessage);
        appLogger.info(logMessage);
    }

    /**
     * Registra un evento de aplicación (no relacionado con usuario específico)
     */
    public void logApplicationEvent(String eventType, String description) {
        String logMessage = String.format(
                "APP_EVENT: type=%s, time=%s, description=%s",
                eventType,
                new Timestamp(System.currentTimeMillis()),
                description);

        securityLogger.info(logMessage);
        appLogger.info(logMessage);
    }

    // =============================================
    // EVENTOS DE AUTENTICACIÓN
    // =============================================

    /**
     * Registra intentos de inicio de sesión
     */
    public void logLoginAttempt(String email, boolean success, String ipAddress) {
        String eventType = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        logSecurityEvent(eventType,
                String.format("Intento de inicio de sesión para usuario %s - %s", email,
                        success ? "EXITOSO" : "FALLIDO"),
                null, ipAddress);
    }

    /**
     * Registra verificación de sesión
     */
    public void logSessionVerification(String email, boolean valid, String ipAddress) {
        String eventType = valid ? "SESSION_VALID" : "SESSION_INVALID";
        logSecurityEvent(eventType,
                String.format("Verificación de sesión para %s - %s", email, valid ? "VÁLIDA" : "INVÁLIDA"),
                null, ipAddress);
    }

    /**
     * Registra fallo en validación de token JWT
     */
    public void logTokenValidationFailure(String email, String reason, String ipAddress) {
        logSecurityEvent("TOKEN_VALIDATION_FAILED",
                String.format("Fallo de validación de token para %s: %s",
                        email != null ? email : "DESCONOCIDO", reason),
                null, ipAddress);
    }

    // =============================================
    // EVENTOS DE REGISTRO Y GESTIÓN DE USUARIOS
    // =============================================

    /**
     * Registra creación de nuevo usuario
     */
    public void logUserRegistration(String email, boolean success, String ipAddress) {
        String eventType = success ? "USER_REGISTRATION_SUCCESS" : "USER_REGISTRATION_FAILED";
        logSecurityEvent(eventType,
                String.format("Registro de usuario %s - %s", email, success ? "EXITOSO" : "FALLIDO"),
                null, ipAddress);
    }

    /**
     * Registra activación de usuario
     */
    public void logUserActivation(Integer userId, Integer adminId, String ipAddress) {
        logSecurityEvent("USER_ACTIVATION",
                String.format("Usuario %d activado por administrador %d", userId, adminId != null ? adminId : 0),
                userId, ipAddress);
    }

    /**
     * Registra desactivación de usuario
     */
    public void logUserDeactivation(Integer userId, Integer adminId, String ipAddress) {
        logSecurityEvent("USER_DEACTIVATION",
                String.format("Usuario %d desactivado por administrador %d", userId, adminId != null ? adminId : 0),
                userId, ipAddress);
    }

    /**
     * Registra aprobación de usuario
     */
    public void logUserApproval(Integer userId, Integer adminId, String ipAddress) {
        logSecurityEvent("USER_APPROVAL",
                String.format("Usuario %d aprobado por administrador %d", userId, adminId != null ? adminId : 0),
                userId, ipAddress);
    }

    /**
     * Registra rechazo de usuario
     */
    public void logUserRejection(Integer userId, Integer adminId, String ipAddress) {
        logSecurityEvent("USER_REJECTION",
                String.format("Usuario %d rechazado por administrador %d", userId, adminId != null ? adminId : 0),
                userId, ipAddress);
    }

    /**
     * Registra desbloqueo de usuario
     */
    public void logUserUnblock(Integer userId, Integer adminId, String ipAddress) {
        logSecurityEvent("USER_UNBLOCK",
                String.format("Usuario %d desbloqueado por administrador %d", userId, adminId != null ? adminId : 0),
                userId, ipAddress);
    }

    /**
     * Registra bloqueo de cuenta por intentos fallidos
     */
    public void logAccountLocked(String email, String ipAddress) {
        logSecurityEvent("ACCOUNT_LOCKED",
                String.format("Cuenta bloqueada para usuario %s por múltiples intentos fallidos", email),
                null, ipAddress);
    }

    // =============================================
    // EVENTOS DE CONTRASEÑA
    // =============================================

    /**
     * Registra cambio de contraseña
     */
    public void logPasswordChange(Integer userId, String ipAddress) {
        logSecurityEvent("PASSWORD_CHANGE",
                "Usuario cambió su contraseña exitosamente",
                userId, ipAddress);
    }

    /**
     * Registra intento de cambio de contraseña
     */
    public void logPasswordChangeAttempt(Integer userId, boolean success, String reason, String ipAddress) {
        String eventType = success ? "PASSWORD_CHANGE_SUCCESS" : "PASSWORD_CHANGE_FAILED";
        logSecurityEvent(eventType,
                String.format("Cambio de contraseña %s%s",
                        success ? "exitoso" : "fallido",
                        reason != null ? ": " + reason : ""),
                userId, ipAddress);
    }

    /**
     * Registra solicitud de recuperación de contraseña
     */
    public void logPasswordRecoveryRequest(String email, String ipAddress) {
        logSecurityEvent("PASSWORD_RECOVERY_REQUEST",
                String.format("Solicitud de recuperación de contraseña para %s", email),
                null, ipAddress);
    }

    /**
     * Registra restablecimiento de contraseña via token
     */
    public void logPasswordRecoveryReset(String email, boolean success, String ipAddress) {
        String eventType = success ? "PASSWORD_RECOVERY_RESET_SUCCESS" : "PASSWORD_RECOVERY_RESET_FAILED";
        logSecurityEvent(eventType,
                String.format("Restablecimiento de contraseña para %s - %s", email, success ? "EXITOSO" : "FALLIDO"),
                null, ipAddress);
    }

    /**
     * Registra cambio forzado de contraseña por admin
     */
    public void logForcedPasswordChange(Integer userId, Integer adminId, String ipAddress) {
        logSecurityEvent("FORCED_PASSWORD_CHANGE",
                String.format("Cambio forzado de contraseña para usuario %d por admin %d", userId,
                        adminId != null ? adminId : 0),
                userId, ipAddress);
    }

    // =============================================
    // EVENTOS DE VERIFICACIÓN DE EMAIL
    // =============================================

    /**
     * Registra verificación de email
     */
    public void logEmailVerification(String email, boolean success, String ipAddress) {
        String eventType = success ? "EMAIL_VERIFICATION_SUCCESS" : "EMAIL_VERIFICATION_FAILED";
        logSecurityEvent(eventType,
                String.format("Verificación de email para %s - %s", email, success ? "EXITOSA" : "FALLIDA"),
                null, ipAddress);
    }

    /**
     * Registra reenvío de email de verificación
     */
    public void logEmailVerificationResent(String email, String ipAddress) {
        logSecurityEvent("EMAIL_VERIFICATION_RESENT",
                String.format("Email de verificación reenviado a %s", email),
                null, ipAddress);
    }

    // =============================================
    // EVENTOS DE CAPTCHA
    // =============================================

    /**
     * Registra validación de CAPTCHA
     */
    public void logCaptchaValidation(String ipAddress, boolean success) {
        String eventType = success ? "CAPTCHA_VALIDATION_SUCCESS" : "CAPTCHA_VALIDATION_FAILED";
        logSecurityEvent(eventType,
                String.format("Validación de CAPTCHA desde IP %s - %s", ipAddress, success ? "EXITOSA" : "FALLIDA"),
                null, ipAddress);
    }

    // =============================================
    // EVENTOS DE CONFIGURACIÓN DE SEGURIDAD
    // =============================================

    /**
     * Registra acceso a configuración de seguridad
     */
    public void logSecurityConfigAccess(Integer userId, String ipAddress) {
        logSecurityEvent("SECURITY_CONFIG_ACCESS",
                "Acceso a configuración de seguridad",
                userId, ipAddress);
    }

    /**
     * Registra cambio en configuración de seguridad
     */
    public void logConfigurationChange(String configKey, String oldValue, String newValue, Integer userId,
            String ipAddress) {
        logSecurityEvent("SECURITY_CONFIG_CHANGE",
                String.format("Configuración %s cambiada de '%s' a '%s'", configKey, oldValue, newValue),
                userId, ipAddress);
    }

    // =============================================
    // EVENTOS DE ROLES Y FUNCIONALIDADES
    // =============================================

    /**
     * Registra creación de rol
     */
    public void logRoleCreated(String roleName, Integer adminId, String ipAddress) {
        logSecurityEvent("ROLE_CREATED",
                String.format("Rol '%s' creado", roleName),
                adminId, ipAddress);
    }

    /**
     * Registra modificación de rol
     */
    public void logRoleModified(Integer roleId, String roleName, Integer adminId, String ipAddress) {
        logSecurityEvent("ROLE_MODIFIED",
                String.format("Rol %d ('%s') modificado", roleId, roleName != null ? roleName : "N/A"),
                adminId, ipAddress);
    }

    /**
     * Registra eliminación de rol
     */
    public void logRoleDeleted(Integer roleId, Integer adminId, String ipAddress) {
        logSecurityEvent("ROLE_DELETED",
                String.format("Rol %d eliminado", roleId),
                adminId, ipAddress);
    }

    /**
     * Registra asignación de funcionalidad a rol
     */
    public void logFunctionalityAssigned(Integer roleId, Integer functionalityId, Integer adminId, String ipAddress) {
        logSecurityEvent("FUNCTIONALITY_ASSIGNED",
                String.format("Funcionalidad %d asignada al rol %d", functionalityId, roleId),
                adminId, ipAddress);
    }

    /**
     * Registra remoción de funcionalidad de rol
     */
    public void logFunctionalityRemoved(Integer roleId, Integer functionalityId, Integer adminId, String ipAddress) {
        logSecurityEvent("FUNCTIONALITY_REMOVED",
                String.format("Funcionalidad %d removida del rol %d", functionalityId, roleId),
                adminId, ipAddress);
    }

    /**
     * Registra creación de funcionalidad
     */
    public void logFunctionalityCreated(String funcName, Integer adminId, String ipAddress) {
        logSecurityEvent("FUNCTIONALITY_CREATED",
                String.format("Funcionalidad '%s' creada", funcName),
                adminId, ipAddress);
    }

    /**
     * Registra eliminación de funcionalidad
     */
    public void logFunctionalityDeleted(Integer funcId, Integer adminId, String ipAddress) {
        logSecurityEvent("FUNCTIONALITY_DELETED",
                String.format("Funcionalidad %d eliminada", funcId),
                adminId, ipAddress);
    }

    // =============================================
    // EVENTOS DE SISTEMA
    // =============================================

    /**
     * Registra inicio de la aplicación
     */
    public void logSystemStartup() {
        logApplicationEvent("SYSTEM_STARTUP", "Microservicio user-service iniciado");
    }

    /**
     * Registra apagado de la aplicación
     */
    public void logSystemShutdown() {
        logApplicationEvent("SYSTEM_SHUTDOWN", "Microservicio user-service detenido");
    }
}