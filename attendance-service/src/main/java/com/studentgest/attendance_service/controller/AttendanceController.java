package com.studentgest.attendance_service.controller;

import com.studentgest.attendance_service.model.Attendance;
import com.studentgest.attendance_service.service.AttendanceService;
import com.studentgest.attendance_service.service.ReportService;
import com.studentgest.attendance_service.service.UserActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asistencia")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserActivityLogger activityLogger;

    // HU-5: Registrar asistencia
    @PostMapping("/registrar")
    public ResponseEntity<?> registerAttendance(@RequestBody Attendance attendance) {
        try {
            System.out.println("📝 Recibido registro de asistencia");

            Attendance saved = attendanceService.registerAttendance(attendance);

            // Log de actividad exitosa
            activityLogger.logAttendanceCreated(
                    attendance.getStudentId(),
                    attendance.getCourseId(),
                    attendance.getTipo(),
                    attendance.getRegisteredBy());

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            // Log de error
            activityLogger.logAttendanceError(
                    attendance.getStudentId(),
                    attendance.getCourseId(),
                    e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Error al registrar asistencia",
                    "message", e.getMessage() != null ? e.getMessage() : "Error desconocido"));
        }
    }

    // HU-6: Justificar ausencia
    @PostMapping("/justificar/{id}")
    public ResponseEntity<Attendance> justifyAbsence(
            @PathVariable Integer id,
            @RequestParam String excusa,
            @RequestParam(required = false) Integer userId) {
        Attendance result = attendanceService.justifyAbsence(id, excusa);

        // Log de actividad
        activityLogger.logAbsenceJustified(id, userId != null ? userId : 0);

        return ResponseEntity.ok(result);
    }

    // HU-7: Reporte de asistencias
    @GetMapping("/reporte")
    public ResponseEntity<Map<String, Long>> getReport(
            @RequestParam Integer curso,
            @RequestParam(required = false) Integer userId) {
        Map<String, Long> report = attendanceService.getAttendanceReport(curso);

        // Log de actividad
        activityLogger.logReportGenerated("RESUMEN_CURSO", curso, userId != null ? userId : 0);

        return ResponseEntity.ok(report);
    }

    // HU-10: Historial por estudiante
    @GetMapping("/historial/{id_estudiante}")
    public ResponseEntity<List<Attendance>> getHistory(
            @PathVariable("id_estudiante") Integer studentId,
            @RequestParam(required = false) Integer userId) {
        List<Attendance> history = attendanceService.getStudentHistory(studentId);

        // Log de actividad
        activityLogger.logHistoryViewed(studentId, userId != null ? userId : 0);

        return ResponseEntity.ok(history);
    }

    // Editar asistencia
    @PutMapping("/editar/{id}")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates) {

        if (!updates.containsKey("tipo")) {
            throw new IllegalArgumentException("El campo 'tipo' es obligatorio");
        }
        if (!updates.containsKey("usuarioId")) {
            throw new IllegalArgumentException("Se requiere ID de usuario");
        }

        // Obtener tipo anterior para el log
        String oldTipo = "desconocido";

        Attendance updateData = new Attendance();
        updateData.setTipo(updates.get("tipo").toString());

        if (updates.containsKey("excusa")) {
            updateData.setExcusa(updates.get("excusa").toString());
        }

        Integer userId = Integer.parseInt(updates.get("usuarioId").toString());
        Attendance result = attendanceService.updateAttendance(id, updateData, userId);

        // Log de actividad
        activityLogger.logAttendanceUpdated(id, oldTipo, updateData.getTipo(), userId);

        return ResponseEntity.ok(result);
    }

    // Eliminar asistencia
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Integer id) {
        attendanceService.deleteAttendance(id);

        // Log de actividad
        activityLogger.logActivity("ASISTENCIA_ELIMINADA", "ID: " + id, true);

        return ResponseEntity.noContent().build();
    }

    // Estadísticas por curso
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam Integer curso,
            @RequestParam(required = false) Integer userId) {
        Map<String, Object> stats = attendanceService.getCourseStats(curso);

        // Log de actividad
        activityLogger.logReportGenerated("ESTADISTICAS", curso, userId != null ? userId : 0);

        return ResponseEntity.ok(stats);
    }

    // Reporte Excel de estudiante
    @GetMapping("/reporte/excel/estudiante")
    public ResponseEntity<ByteArrayResource> getStudentExcelReport(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            @RequestParam(required = false) Integer userId) throws Exception {

        ByteArrayResource file = reportService.generateStudentExcel(studentId, courseId, from, to);

        // Log de actividad
        activityLogger.logReportGenerated("EXCEL_ESTUDIANTE", courseId, userId != null ? userId : 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_asistencia_estudiante.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    // Reporte Excel de curso
    @GetMapping("/reporte/excel/curso")
    public ResponseEntity<ByteArrayResource> getCourseSummaryExcel(
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            @RequestParam(required = false) Integer userId) throws Exception {

        ByteArrayResource file = reportService.generateCourseSummaryExcel(courseId, from, to);

        // Log de actividad
        activityLogger.logReportGenerated("EXCEL_CURSO", courseId, userId != null ? userId : 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resumen_curso.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    // Reporte PDF
    @GetMapping("/reporte/pdf/acta")
    public ResponseEntity<ByteArrayResource> getStudentActaPdf(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            @RequestParam(required = false) Integer userId) throws Exception {

        ByteArrayResource file = reportService.generateStudentHistoryPdf(studentId, courseId, from, to);

        // Log de actividad
        activityLogger.logReportGenerated("PDF_HISTORIAL", courseId, userId != null ? userId : 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=historial_asistencia_" + studentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    // Registro completo por curso
    @GetMapping("/course/{courseId}/record")
    public ResponseEntity<Map<Integer, List<Attendance>>> getFullAttendanceRecord(@PathVariable Integer courseId) {
        Map<Integer, List<Attendance>> record = attendanceService.getFullAttendanceRecord(courseId);

        // Log de actividad
        activityLogger.logActivity("REGISTRO_CURSO_CONSULTADO", "Curso: " + courseId, true);

        return ResponseEntity.ok(record);
    }

    // Asistencias ordenadas por estudiante
    @GetMapping("/curso/{courseId}")
    public ResponseEntity<List<Attendance>> getCourseAttendancesOrderedByStudent(@PathVariable Integer courseId) {
        List<Attendance> attendances = attendanceService.getAttendancesByCourseOrderedByStudent(courseId);

        // Log de actividad
        activityLogger.logActivity("ASISTENCIAS_CURSO_CONSULTADAS",
                "Curso: " + courseId + " | Registros: " + attendances.size(), true);

        return ResponseEntity.ok(attendances);
    }
}