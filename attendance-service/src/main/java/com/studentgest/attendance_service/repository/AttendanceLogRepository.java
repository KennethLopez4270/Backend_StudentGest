package com.studentgest.attendance_service.repository;

import com.studentgest.attendance_service.model.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Integer> {
    List<AttendanceLog> findByAttendanceId(Integer attendanceId);
}