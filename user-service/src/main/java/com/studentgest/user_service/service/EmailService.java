package com.studentgest.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Transport;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:studentgest.app@gmail.com}")
    private String fromEmail;
    
    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;
    
    @Value("${spring.mail.port:587}")
    private String mailPort;
    
    /**
     * Envía email de verificación con manejo robusto de errores y logs detallados
     */
    public boolean sendVerificationEmail(String to, String name, String verificationLink) {
        logger.info("═══════════════════════════════════════════════════");
        logger.info("🚀 INICIANDO ENVÍO DE EMAIL DE VERIFICACIÓN");
        logger.info("═══════════════════════════════════════════════════");
        logger.info("📧 Destinatario: {}", to);
        logger.info("👤 Nombre: {}", name);
        logger.info("🔗 Enlace: {}", verificationLink);
        logger.info("📨 Remitente: {}", fromEmail);
        logger.info("🏠 SMTP Host: {}:{}", mailHost, mailPort);
        
        boolean htmlSuccess = false;
        boolean textSuccess = false;
        
        try {
            // ✅ PRIMERO intentar con HTML
            logger.info("🔄 Intentando envío con formato HTML...");
            htmlSuccess = sendVerificationEmailHtml(to, name, verificationLink);
            
            if (htmlSuccess) {
                logger.info("✅ Email HTML enviado exitosamente");
                return true;
            } else {
                logger.warn("⚠️ Falló email HTML, intentando con texto plano...");
                // ✅ FALLBACK a texto plano
                textSuccess = sendVerificationEmailPlainText(to, name, verificationLink);
                
                if (textSuccess) {
                    logger.info("✅ Email de texto plano enviado exitosamente");
                    return true;
                } else {
                    logger.error("❌ Ambos métodos fallaron");
                    return false;
                }
            }
            
        } catch (Exception e) {
            logger.error("💥 ERROR CRÍTICO en sendVerificationEmail: {}", e.getMessage(), e);
            return false;
        } finally {
            logger.info("📊 RESUMEN - HTML: {}, Texto: {}", 
                       htmlSuccess ? "✅" : "❌", 
                       textSuccess ? "✅" : "❌");
            logger.info("═══════════════════════════════════════════════════");
        }
    }
    
    /**
     * Envía email de verificación con formato HTML mejorado
     */
    private boolean sendVerificationEmailHtml(String to, String name, String verificationLink) {
        try {
            logger.info("🎨 Preparando email HTML para: {}", to);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "StudentGest - Sistema de Gestión Estudiantil");
            helper.setTo(to);
            helper.setSubject("✅ Verifica tu cuenta - StudentGest");
            
            // Email con formato HTML profesional
            String htmlContent = buildVerificationEmailHtml(name, verificationLink);
            helper.setText(htmlContent, true);
            
            logger.info("📤 Enviando email HTML...");
            mailSender.send(message);
            
            logger.info("🎉 Email HTML enviado EXITOSAMENTE a: {}", to);
            logger.info("🔗 Enlace de verificación enviado: {}", verificationLink);
            return true;
            
        } catch (MessagingException e) {
            logger.error("❌ Error de mensajería HTML para {}: {}", to, e.getMessage());
            logger.debug("🔧 Detalles técnicos:", e);
            return false;
        } catch (Exception e) {
            logger.error("💥 Error inesperado en email HTML para {}: {}", to, e.getMessage());
            return false;
        }
    }
    
    /**
     * Construye el contenido HTML del email de verificación
     */
    private String buildVerificationEmailHtml(String name, String verificationLink) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verifica tu cuenta - StudentGest</title>
                <style>
                    body {
                        font-family: 'Arial', sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    .content {
                        padding: 30px;
                    }
                    .verification-button {
                        display: inline-block;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 15px 30px;
                        text-decoration: none;
                        border-radius: 25px;
                        font-weight: bold;
                        font-size: 16px;
                        margin: 20px 0;
                        text-align: center;
                    }
                    .verification-link {
                        word-break: break-all;
                        background: #f8f9fa;
                        padding: 15px;
                        border-radius: 5px;
                        border-left: 4px solid #667eea;
                        margin: 15px 0;
                        font-size: 14px;
                        color: #666;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        color: #666;
                        font-size: 12px;
                        border-top: 1px solid #e9ecef;
                    }
                    .security-note {
                        background: #fff3cd;
                        border: 1px solid #ffeaa7;
                        border-radius: 5px;
                        padding: 15px;
                        margin: 20px 0;
                        color: #856404;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 StudentGest</h1>
                        <p>Sistema de Gestión Estudiantil</p>
                    </div>
                    
                    <div class="content">
                        <h2>¡Hola, %s!</h2>
                        <p>Gracias por registrarte en <strong>StudentGest</strong>. Estamos emocionados de tenerte con nosotros.</p>
                        
                        <p>Para activar tu cuenta y comenzar a utilizar todos nuestros servicios, necesitas verificar tu dirección de email.</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="verification-button">
                                ✅ Verificar Mi Cuenta
                            </a>
                        </div>
                        
                        <p>Si el botón no funciona, copia y pega el siguiente enlace en tu navegador:</p>
                        
                        <div class="verification-link">
                            %s
                        </div>
                        
                        <div class="security-note">
                            <strong>⚠️ Importante:</strong> 
                            <ul>
                                <li>Este enlace expirará en <strong>24 horas</strong></li>
                                <li>No compartas este enlace con nadie</li>
                                <li>Si no te registraste en StudentGest, ignora este mensaje</li>
                            </ul>
                        </div>
                        
                        <p>Una vez verificado tu email, tu cuenta estará lista para ser aprobada por el administrador.</p>
                        
                        <p>¡Nos vemos dentro!</p>
                        <p><strong>El equipo de StudentGest</strong></p>
                    </div>
                    
                    <div class="footer">
                        <p>© 2024 StudentGest. Todos los derechos reservados.</p>
                        <p>Este es un email automático, por favor no respondas a este mensaje.</p>
                        <p>Si necesitas ayuda, contacta al administrador del sistema.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, verificationLink, verificationLink);
    }
    
    /**
     * Método alternativo con email de texto plano (fallback)
     */
    private boolean sendVerificationEmailPlainText(String to, String name, String verificationLink) {
        try {
            logger.info("📝 Preparando email de texto plano para: {}", to);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Verifica tu cuenta - StudentGest");
            
            String textContent = buildVerificationEmailText(name, verificationLink);
            message.setText(textContent);
            
            logger.info("📤 Enviando email de texto plano...");
            mailSender.send(message);
            
            logger.info("✅ Email de texto plano enviado a: {}", to);
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Error enviando email de texto plano a {}: {}", to, e.getMessage());
            return false;
        }
    }
    
    /**
     * Construye el contenido de texto del email de verificación
     */
    private String buildVerificationEmailText(String name, String verificationLink) {
        return """
            Hola %s,

            ¡Bienvenido a StudentGest!

            Gracias por registrarte en nuestro sistema de gestión estudiantil. 
            Para activar tu cuenta, por favor haz clic en el siguiente enlace:

            %s

            ⚠️ IMPORTANTE:
            • Este enlace expirará en 24 horas
            • No compartas este enlace con nadie
            • Si no te registraste en StudentGest, ignora este mensaje

            Una vez verificado tu email, tu cuenta estará lista para ser aprobada 
            por el administrador.

            ¡Nos vemos dentro!

            Saludos,
            El equipo de StudentGest
            🎓 Sistema de Gestión Estudiantil

            ---------------------------------
            Este es un email automático, por favor no respondas a este mensaje.
            Si necesitas ayuda, contacta al administrador del sistema.
            """.formatted(name, verificationLink);
    }
    
    /**
     * Verifica la configuración del servicio de email
     */
    public boolean testEmailConfiguration(String testEmail) {
        logger.info("═══════════════════════════════════════════════════");
        logger.info("🧪 INICIANDO PRUEBA DE CONFIGURACIÓN DE EMAIL");
        logger.info("═══════════════════════════════════════════════════");
        logger.info("📧 Email de prueba: {}", testEmail);
        logger.info("📨 Remitente configurado: {}", fromEmail);
        logger.info("🏠 SMTP: {}:{}", mailHost, mailPort);
        
        try {
            // Probar conexión SMTP básica
            logger.info("🔍 Probando configuración SMTP...");
            
            // Enviar un email de prueba simple
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(fromEmail);
            testMessage.setTo(testEmail);
            testMessage.setSubject("🧪 Prueba de Configuración - StudentGest");
            testMessage.setText("""
                Este es un email de prueba para verificar la configuración del servicio de email de StudentGest.
                
                Si recibes este mensaje, la configuración es correcta.
                
                ✅ Configuración de email funcionando correctamente.
                
                Saludos,
                Equipo StudentGest
                """);
            
            logger.info("📤 Enviando email de prueba...");
            mailSender.send(testMessage);
            
            logger.info("🎉 PRUEBA EXITOSA - Email de prueba enviado a: {}", testEmail);
            logger.info("✅ CONFIGURACIÓN DE EMAIL VERIFICADA");
            logger.info("═══════════════════════════════════════════════════");
            return true;
            
        } catch (Exception e) {
            logger.error("❌ PRUEBA FALLIDA - Error en configuración de email: {}", e.getMessage());
            logger.error("💡 DIAGNÓSTICO Y SOLUCIONES:");
            logger.error("   1. 📧 Verifica spring.mail.username: {}", fromEmail);
            logger.error("   2. 🔐 Verifica spring.mail.password: [CONTRASEÑA_OCULTA]");
            logger.error("   3. 🌐 Verifica que uses 'App Password' de Google (16 caracteres sin espacios)");
            logger.error("   4. ⚙️  Verifica que la verificación en 2 pasos esté activada en Google");
            logger.error("   5. 🚪 Verifica que el puerto {} no esté bloqueado", mailPort);
            logger.error("   6. 🔗 Verifica conexión a Internet");
            logger.error("   7. 🐛 Error específico: {}", e.getMessage());
            logger.info("═══════════════════════════════════════════════════");
            return false;
        }
    }
    
    /**
     * Método sobrecargado para prueba sin email específico
     */
    public boolean testEmailConfiguration() {
        return testEmailConfiguration("test@studentgest.com");
    }
    
    /**
     * Método para mostrar el enlace en logs (útil para desarrollo)
     */
    public void logVerificationLink(String to, String name, String verificationLink) {
        logger.info("═══════════════════════════════════════════════════");
        logger.info("🔗 ENLACE DE VERIFICACIÓN (PARA DESARROLLO)");
        logger.info("═══════════════════════════════════════════════════");
        logger.info("Para: {}", to);
        logger.info("Nombre: {}", name);
        logger.info("📋 COPIAR Y PEGAR ESTE ENLACE EN EL NAVEGADOR:");
        logger.info("   {}", verificationLink);
        logger.info("═══════════════════════════════════════════════════");
        logger.info("💡 En producción, este enlace se enviaría por email");
        logger.info("═══════════════════════════════════════════════════");
    }
    
    /**
     * Método que intenta enviar email real y si falla, muestra el enlace en logs
     */
    public boolean sendVerificationEmailWithFallback(String to, String name, String verificationLink) {
        boolean emailSent = sendVerificationEmail(to, name, verificationLink);
        
        if (!emailSent) {
            logger.warn("⚠️ Falló el envío de email real, mostrando enlace en logs...");
            logVerificationLink(to, name, verificationLink);
            // En desarrollo, consideramos esto como "éxito" para continuar el flujo
            return true;
        }
        
        return emailSent;
    }
    // Asegúrate de tener esta inyección en tu EmailService
@Autowired
private JavaMailSender javaMailSender;

// Agregar estos métodos a tu EmailService existente
public boolean sendPasswordRecoveryEmail(String toEmail, String userName, String recoveryLink) {
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Recuperación de Contraseña - StudentGest");
        message.setText(
            "Hola " + userName + ",\n\n" +
            "Has solicitado restablecer tu contraseña en StudentGest.\n\n" +
            "Para restablecer tu contraseña, haz clic en el siguiente enlace:\n" +
            recoveryLink + "\n\n" +
            "Este enlace expirará en 24 horas.\n\n" +
            "Si no solicitaste este cambio, puedes ignorar este mensaje.\n\n" +
            "Saludos,\nEquipo StudentGest"
        );
        
        javaMailSender.send(message);
        return true;
    } catch (Exception e) {
        logger.error("Error enviando email de recuperación: {}", e.getMessage());
        return false;
    }
}

public boolean sendPasswordChangedNotification(String toEmail, String userName) {
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Contraseña Actualizada - StudentGest");
        message.setText(
            "Hola " + userName + ",\n\n" +
            "Tu contraseña en StudentGest ha sido actualizada exitosamente.\n\n" +
            "Si no realizaste este cambio, por favor contacta inmediatamente al administrador.\n\n" +
            "Saludos,\nEquipo StudentGest"
        );
        
        javaMailSender.send(message);
        return true;
    } catch (Exception e) {
        logger.error("Error enviando notificación de cambio de contraseña: {}", e.getMessage());
        return false;
    }
}
}