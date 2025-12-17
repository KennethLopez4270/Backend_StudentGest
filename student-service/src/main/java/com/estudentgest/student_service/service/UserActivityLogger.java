package com.estudentgest.student_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserActivityLogger {

    private static final Logger activityLogger = LoggerFactory.getLogger("USER_ACTIVITY");

    /**
     * Registra la consulta de cursos del profesor
     */
    public void logCoursesViewed(Long professorId, int courseCount) {
        String message = String.format(
                "CURSOS_PROFESOR_CONSULTADOS | Profesor ID: %d | Cursos encontrados: %d | Resultado: Exitoso",
                professorId, courseCount);
        activityLogger.info(message);
    }

    /**
     * Registra la consulta de estudiantes de un curso
     */
    public void logStudentsViewed(Long courseId, int studentCount) {
        String message = String.format(
                "ESTUDIANTES_CURSO_CONSULTADOS | Curso ID: %d | Estudiantes encontrados: %d | Resultado: Exitoso",
                courseId, studentCount);
        activityLogger.info(message);
    }

    /**
     * Registra la consulta de hijos de un padre
     */
    public void logChildrenViewed(Long parentId, int childCount) {
        String message = String.format(
                "HIJOS_PADRE_CONSULTADOS | Padre ID: %d | Hijos encontrados: %d | Resultado: Exitoso",
                parentId, childCount);
        activityLogger.info(message);
    }

    /**
     * Registra la conexión padre-estudiante
     */
    public void logParentChildConnected(Long parentId, Long studentId) {
        String message = String.format(
                "PADRE_ESTUDIANTE_CONECTADO | Padre ID: %d | Estudiante ID: %d | Resultado: Exitoso",
                parentId, studentId);
        activityLogger.info(message);
    }

    /**
     * Registra la consulta de un curso específico
     */
    public void logCourseViewed(Long courseId) {
        String message = String.format(
                "CURSO_CONSULTADO | Curso ID: %d | Resultado: Exitoso",
                courseId);
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

    /**
     * Log de error
     */
    public void logError(String action, String error) {
        String message = String.format(
                "%s | Error: %s | Resultado: Fallido",
                action, error);
        activityLogger.error(message);
    }
}
