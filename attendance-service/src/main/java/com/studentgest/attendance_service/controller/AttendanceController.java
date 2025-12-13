package com.studentgest.attendance_service.controller;

import com.studentgest.attendance_service.model.Attendance;
import com.studentgest.attendance_service.service.AttendanceService;
import com.studentgest.attendance_service.service.ReportService;
import jakarta.annotation.Resource;
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

    // HU-5
    @PostMapping("/registrar")
    public ResponseEntity<Attendance> registerAttendance(@RequestBody Attendance attendance) {
        return ResponseEntity.ok(attendanceService.registerAttendance(attendance));
    }

    // HU-6
    @PostMapping("/justificar/{id}")
    public ResponseEntity<Attendance> justifyAbsence(
            @PathVariable Integer id,
            @RequestParam String excusa) {
        return ResponseEntity.ok(attendanceService.justifyAbsence(id, excusa));
    }

    // HU-7
    @GetMapping("/reporte")
    public ResponseEntity<Map<String, Long>> getReport(@RequestParam Integer curso) {
        return ResponseEntity.ok(attendanceService.getAttendanceReport(curso));
    }

    // HU-10
    @GetMapping("/historial/{id_estudiante}")
    public ResponseEntity<List<Attendance>> getHistory(
            @PathVariable("id_estudiante") Integer studentId) {
        return ResponseEntity.ok(attendanceService.getStudentHistory(studentId));
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates) {  // Cambiado a Map<String, Object>

        // Validaciones básicas
        if (!updates.containsKey("tipo")) {
            throw new IllegalArgumentException("El campo 'tipo' es obligatorio");
        }
        if (!updates.containsKey("usuarioId")) {
            throw new IllegalArgumentException("Se requiere ID de usuario");
        }

        // Crear objeto con los datos de actualización
        Attendance updateData = new Attendance();
        updateData.setTipo(updates.get("tipo").toString());

        if (updates.containsKey("excusa")) {
            updateData.setExcusa(updates.get("excusa").toString());
        }

        // Obtener ID de usuario
        Integer userId = Integer.parseInt(updates.get("usuarioId").toString());

        // Llamar al servicio
        Attendance result = attendanceService.updateAttendance(id, updateData, userId);
        return ResponseEntity.ok(result);
    }

    // Extra: Eliminar
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Integer id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }

    // Extra: Estadísticas
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam Integer curso) {
        return ResponseEntity.ok(attendanceService.getCourseStats(curso));
    }

    @GetMapping("/reporte/excel/estudiante")
    public ResponseEntity<ByteArrayResource> getStudentExcelReport(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to
    ) throws Exception {
        ByteArrayResource file = reportService.generateStudentExcel(studentId, courseId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_asistencia_estudiante.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/reporte/excel/curso")
    public ResponseEntity<ByteArrayResource> getCourseSummaryExcel(
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to
    ) throws Exception {
        ByteArrayResource file = reportService.generateCourseSummaryExcel(courseId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resumen_curso.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/reporte/pdf/acta")
    public ResponseEntity<ByteArrayResource> getStudentActaPdf(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to
    ) throws Exception {
        ByteArrayResource file = reportService.generateStudentHistoryPdf(studentId, courseId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historial_asistencia_" + studentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }
    // En AttendanceController.java

    // En AttendanceController.java
    @GetMapping("/course/{courseId}/record")
    public ResponseEntity<Map<Integer, List<Attendance>>> getFullAttendanceRecord(@PathVariable Integer courseId) {
        return ResponseEntity.ok(attendanceService.getFullAttendanceRecord(courseId));
    }

    @GetMapping("/curso/{courseId}")
    public ResponseEntity<List<Attendance>> getCourseAttendancesOrderedByStudent(
            @PathVariable Integer courseId) {
        List<Attendance> attendances = attendanceService.getAttendancesByCourseOrderedByStudent(courseId);
        return ResponseEntity.ok(attendances);
    }

}