package com.studentgest.user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class AuditLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    
    public void logSecurityEvent(String eventType, String description, Integer userId, String ipAddress) {
        String logMessage = String.format(
            "SECURITY_EVENT: type=%s, userId=%d, ip=%s, time=%s, description=%s",
            eventType, userId, ipAddress, new Timestamp(System.currentTimeMillis()), description
        );
        
        logger.info(logMessage);
    }
    
    public void logLoginAttempt(String email, boolean success, String ipAddress) {
        String eventType = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        logSecurityEvent(eventType, 
            String.format("Login attempt for user %s - %s", email, success ? "SUCCESS" : "FAILED"), 
            null, ipAddress);
    }
    
    public void logPasswordChange(Integer userId, String ipAddress) {
        logSecurityEvent("PASSWORD_CHANGE", "User changed password", userId, ipAddress);
    }
    
    public void logAccountLocked(String email, String ipAddress) {
        logSecurityEvent("ACCOUNT_LOCKED", 
            String.format("Account locked for user %s due to multiple failed attempts", email), 
            null, ipAddress);
    }
}