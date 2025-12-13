package com.studentgest.attendance_service.report;


import com.studentgest.attendance_service.model.Attendance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExcelReportGenerator {

    public ByteArrayResource generateStudentReport(List<Attendance> attendances) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Asistencia Detallada");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // Header
            String[] columns = {"Fecha", "Tipo", "Excusa"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Body
            int rowNum = 1;
            for (Attendance att : attendances) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(sdf.format(att.getFecha()));
                row.createCell(1).setCellValue(att.getTipo());
                row.createCell(2).setCellValue(att.getExcusa() != null ? att.getExcusa() : "");
            }

            // Totales
            Map<String, Long> totals = attendances.stream()
                    .collect(Collectors.groupingBy(Attendance::getTipo, Collectors.counting()));

            Row totalRow = sheet.createRow(rowNum + 1);
            totalRow.createCell(0).setCellValue("Resumen");

            int col = 1;
            for (Map.Entry<String, Long> entry : totals.entrySet()) {
                totalRow.createCell(col++).setCellValue(entry.getKey() + ": " + entry.getValue());
            }

            // Export
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        }
    }

    public ByteArrayResource generateCourseSummary(List<Attendance> attendances) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Resumen por Curso");

            // Crear encabezado
            String[] columns = {"ID Estudiante", "Presente", "Ausente", "Tardanza", "Total", "% Asistencia"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            // Agrupar por estudiante y contar asistencias
            Map<Integer, Map<String, Long>> resumen = attendances.stream()
                    .collect(Collectors.groupingBy(
                            Attendance::getStudentId,
                            Collectors.groupingBy(Attendance::getTipo, Collectors.counting())
                    ));

            int rowNum = 1;
            for (Map.Entry<Integer, Map<String, Long>> entry : resumen.entrySet()) {
                Integer studentId = entry.getKey();
                Map<String, Long> tipos = entry.getValue();

                long presente = tipos.getOrDefault("presente", 0L);
                long ausente = tipos.getOrDefault("ausente", 0L);
                long tardanza = tipos.getOrDefault("tardanza", 0L);
                long total = presente + ausente + tardanza;
                double porcentaje = total == 0 ? 0 : (double) presente / total * 100;

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(studentId);
                row.createCell(1).setCellValue(presente);
                row.createCell(2).setCellValue(ausente);
                row.createCell(3).setCellValue(tardanza);
                row.createCell(4).setCellValue(total);
                row.createCell(5).setCellValue(porcentaje);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        }
    }

}
