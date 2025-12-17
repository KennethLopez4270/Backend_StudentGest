package com.studentgest.attendance_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserActivityLogger {

    // Logger específico para actividad de usuario
    private static final Logger activityLogger = LoggerFactory.getLogger("USER_ACTIVITY");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Registra la creación de una asistencia
     */
    public void logAttendanceCreated(Integer studentId, Integer courseId, String tipo, Integer registeredBy) {
        String message = String.format(
                "ASISTENCIA_CREADA | Estudiante: %d | Curso: %d | Tipo: %s | Registrado por: %d | Resultado: Exitoso",
                studentId, courseId, tipo, registeredBy);
        activityLogger.info(message);
    }

    /**
     * Registra un error al crear asistencia
     */
    public void logAttendanceError(Integer studentId, Integer courseId, String error) {
        String message = String.format(
                "ASISTENCIA_ERROR | Estudiante: %d | Curso: %d | Error: %s | Resultado: Fallido",
                studentId, courseId, error);
        activityLogger.error(message);
    }

    /**
     * Registra la consulta de cursos por profesor
     */
    public void logCoursesViewed(Integer professorId, int courseCount) {
        String message = String.format(
                "CURSOS_CONSULTADOS | Profesor: %d | Cursos encontrados: %d | Resultado: Exitoso",
                professorId, courseCount);
        activityLogger.info(message);
    }

    /**
     * Registra la justificación de una ausencia
     */
    public void logAbsenceJustified(Integer attendanceId, Integer justifiedBy) {
        String message = String.format(
                "AUSENCIA_JUSTIFICADA | Asistencia ID: %d | Justificado por: %d | Resultado: Exitoso",
                attendanceId, justifiedBy);
        activityLogger.info(message);
    }

    /**
     * Registra la edición de una asistencia
     */
    public void logAttendanceUpdated(Integer attendanceId, String oldTipo, String newTipo, Integer modifiedBy) {
        String message = String.format(
                "ASISTENCIA_MODIFICADA | Asistencia ID: %d | Anterior: %s | Nuevo: %s | Modificado por: %d | Resultado: Exitoso",
                attendanceId, oldTipo, newTipo, modifiedBy);
        activityLogger.info(message);
    }

    /**
     * Registra la generación de un reporte
     */
    public void logReportGenerated(String reportType, Integer courseId, Integer userId) {
        String message = String.format(
                "REPORTE_GENERADO | Tipo: %s | Curso: %d | Solicitado por: %d | Resultado: Exitoso",
                reportType, courseId, userId);
        activityLogger.info(message);
    }

    /**
     * Registra la consulta del historial de un estudiante
     */
    public void logHistoryViewed(Integer studentId, Integer viewedBy) {
        String message = String.format(
                "HISTORIAL_CONSULTADO | Estudiante: %d | Consultado por: %d | Resultado: Exitoso",
                studentId, viewedBy);
        activityLogger.info(message);
    }

    /**
     * Log genérico de actividad
     */
    public void logActivity(String action, String details, boolean success) {
        String message = String.format(
                "%s | %s | Resultado: %s",
                action, details, success ? "Exitoso" : "Fallido");
        if (success) {
            activityLogger.info(message);
        } else {
            activityLogger.warn(message);
        }
    }
}
