package com.studentgest.attendance_service.repository;

import com.studentgest.attendance_service.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> findByStudentId(Integer studentId);
    List<Attendance> findByCourseId(Integer courseId);
    // Para la opción 2 (fechas recientes primero)
    List<Attendance> findByCourseIdOrderByFechaAscStudentIdAsc(Integer courseId);
    List<Attendance> findAllByOrderByFechaAscStudentIdAsc();
    List<Attendance> findByCourseIdOrderByStudentIdAscFechaAsc(Integer courseId);
    List<Attendance> findAllByOrderByStudentIdAscFechaAsc();
    List<Attendance> findByCourseIdOrderByStudentIdAsc(Integer courseId);
}