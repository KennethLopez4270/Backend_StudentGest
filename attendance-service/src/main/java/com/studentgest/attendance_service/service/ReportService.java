package com.studentgest.attendance_service.service;

import com.studentgest.attendance_service.model.Attendance;
import com.studentgest.attendance_service.report.PdfReportGenerator;
import com.studentgest.attendance_service.repository.AttendanceRepository;
import com.studentgest.attendance_service.report.ExcelReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ExcelReportGenerator excelReportGenerator;

    @Autowired
    private PdfReportGenerator pdfReportGenerator;

    @Autowired
    private ReportService reportService;

    public ByteArrayResource generateStudentExcel(Integer studentId, Integer courseId, Date from, Date to) throws Exception {
        List<Attendance> all = attendanceRepository.findByStudentId(studentId);
        List<Attendance> filtered = all.stream()
                .filter(a -> a.getCourseId().equals(courseId)
                        && !a.getFecha().before(from)
                        && !a.getFecha().after(to))
                .collect(Collectors.toList());

        return excelReportGenerator.generateStudentReport(filtered);
    }

    public ByteArrayResource generateCourseSummaryExcel(Integer courseId, Date from, Date to) throws Exception {
        List<Attendance> attendances = attendanceRepository.findByCourseId(courseId);

        List<Attendance> filtered = attendances.stream()
                .filter(a -> !a.getFecha().before(from) && !a.getFecha().after(to))
                .collect(Collectors.toList());

        return excelReportGenerator.generateCourseSummary(filtered);
    }
    public ByteArrayResource generateStudentHistoryPdf(Integer studentId, Integer courseId, java.util.Date from, java.util.Date to) throws Exception {
        List<Attendance> all = attendanceRepository.findByStudentId(studentId);

        List<Attendance> filtered = all.stream()
                .filter(a -> a.getCourseId().equals(courseId)
                        && !a.getFecha().before(from)
                        && !a.getFecha().after(to))
                .sorted((a1, a2) -> a1.getFecha().compareTo(a2.getFecha()))
                .collect(Collectors.toList());

        return pdfReportGenerator.generateStudentHistoryPdf(filtered, studentId, courseId);
    }

    public ByteArrayResource generateCourseStatsPdf(Integer courseId, Date from, Date to) throws Exception {
        List<Attendance> attendances = attendanceRepository.findByCourseId(courseId);
        List<Attendance> filtered = attendances.stream()
                .filter(a -> !a.getFecha().before(from) && !a.getFecha().after(to))
                .collect(Collectors.toList());

        return pdfReportGenerator.generateCourseStatsPdf(filtered, courseId);
    }

    @GetMapping("/reporte/pdf/estadisticas")
    public ResponseEntity<ByteArrayResource> getCourseStatsPdf(
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to
    ) throws Exception {
        ByteArrayResource file = reportService.generateCourseStatsPdf(courseId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=estadisticas_asistencia_" + courseId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }



}
