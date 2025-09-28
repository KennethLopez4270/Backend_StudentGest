package com.studentgest.attendance_service.service;

import com.studentgest.attendance_service.model.Attendance;
import com.studentgest.attendance_service.model.AttendanceLog;
import com.studentgest.attendance_service.repository.AttendanceLogRepository;
import com.studentgest.attendance_service.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;  // ¡Importación añadida!
import java.util.stream.Collectors;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    // HU-5: Registrar asistencia
    public Attendance registerAttendance(Attendance attendance) {
        attendance.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return attendanceRepository.save(attendance);
    }

    // HU-6: Justificar ausencia
    public Attendance justifyAbsence(Integer id, String excuse) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));

        // Guardar historial antes de modificar
        saveLog(attendance, "excusa", attendance.getExcusa(), excuse);

        attendance.setExcusa(excuse);
        attendance.setTipo("ausente");
        return attendanceRepository.save(attendance);
    }

    // HU-7: Reporte de asistencias por curso
    public Map<String, Long> getAttendanceReport(Integer courseId) {
        List<Attendance> attendances = attendanceRepository.findByCourseId(courseId);
        return attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getTipo, Collectors.counting()));
    }

    // HU-10: Historial por estudiante
    public List<Attendance> getStudentHistory(Integer studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    public Attendance updateAttendance(Integer id, Attendance updatedAttendance, Integer userId) {
        // Validaciones
        if (id == null || userId == null) {
            throw new IllegalArgumentException("Datos inválidos");
        }

        Attendance existing = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));

        // Registrar cambios en tipo
        if (!existing.getTipo().equals(updatedAttendance.getTipo())) {
            saveLog(existing, "tipo", existing.getTipo(), updatedAttendance.getTipo(), userId);
            existing.setTipo(updatedAttendance.getTipo());
        }

        // Registrar cambios en excusa (si viene en el update)
        if (updatedAttendance.getExcusa() != null &&
                !updatedAttendance.getExcusa().equals(existing.getExcusa())) {
            saveLog(existing, "excusa", existing.getExcusa(), updatedAttendance.getExcusa(), userId);
            existing.setExcusa(updatedAttendance.getExcusa());
        }

        return attendanceRepository.save(existing);
    }
    private void saveLog(Attendance attendance, String field,
                         String oldValue, String newValue, Integer modifiedBy) {
        AttendanceLog log = new AttendanceLog();
        log.setAttendanceId(attendance.getId_asistencia());
        log.setModifiedBy(modifiedBy);  // Usar el ID del usuario que modifica
        log.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        if ("tipo".equals(field)) {
            log.setTipo_anterior(oldValue);
            log.setTipo_nuevo(newValue);
        } else {
            log.setExcusa_anterior(oldValue);
            log.setExcusa_nueva(newValue);
        }

        attendanceLogRepository.save(log);
    }
    // Método auxiliar para detectar cambios
    private boolean hasChanged(String oldValue, String newValue) {
        if (oldValue == null) {
            return newValue != null;
        }
        return !oldValue.equals(newValue);
    }

    // Endpoint extra: Eliminar asistencia
    public void deleteAttendance(Integer id) {
        attendanceRepository.deleteById(id);
    }

    // Endpoint extra: Estadísticas por curso
    public Map<String, Object> getCourseStats(Integer courseId) {
        List<Attendance> attendances = attendanceRepository.findByCourseId(courseId);
        long total = attendances.size();
        long present = attendances.stream().filter(a -> a.getTipo().equals("presente")).count();

        return Map.of(
                "total", total,
                "present", present,
                "absent", total - present,
                "attendanceRate", (double) present / total * 100
        );
    }

    // Método auxiliar para guardar en el historial
    private void saveLog(Attendance attendance, String field, String oldValue, String newValue) {
        AttendanceLog log = new AttendanceLog();
        log.setAttendanceId(attendance.getId_asistencia());
        log.setModifiedBy(attendance.getRegisteredBy());
        log.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        if (field.equals("tipo")) {
            log.setTipo_anterior(oldValue);
            log.setTipo_nuevo(newValue);
        } else {
            log.setExcusa_anterior(oldValue);
            log.setExcusa_nueva(newValue);
        }

        attendanceLogRepository.save(log);
    }

    // Método para obtener registros agrupados por estudiante
    public Map<Integer, List<Attendance>> getFullAttendanceRecord(Integer courseId) {
        List<Attendance> attendances = attendanceRepository.findByCourseId(courseId);

        return attendances.stream()
                .collect(Collectors.groupingBy(
                        Attendance::getStudentId,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }
    public List<Attendance> getAttendancesOrderedByStudent(Integer courseId) {
        if (courseId != null) {
            return attendanceRepository.findByCourseIdOrderByStudentIdAscFechaAsc(courseId);
        }
        return attendanceRepository.findAllByOrderByStudentIdAscFechaAsc();
    }

    // Método para obtener asistencias ordenadas
    public List<Attendance> getAllAttendancesOrdered(Integer courseId) {
        if (courseId != null) {
            return attendanceRepository.findByCourseIdOrderByFechaAscStudentIdAsc(courseId);
        }
        return attendanceRepository.findAllByOrderByFechaAscStudentIdAsc();
    }

    public List<Attendance> getAttendancesByCourseOrderedByStudent(Integer courseId) {
        return attendanceRepository.findByCourseIdOrderByStudentIdAsc(courseId);
    }
}