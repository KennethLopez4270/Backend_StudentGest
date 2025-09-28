package com.studentgest.attendance_service.report;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;

import com.itextpdf.layout.properties.UnitValue;
import com.studentgest.attendance_service.model.Attendance;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PdfReportGenerator {

    public ByteArrayResource generateStudentHistoryPdf(List<Attendance> attendances, Integer studentId, Integer courseId) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        doc.add(new Paragraph("Historial de Asistencia")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(16));

        doc.add(new Paragraph("ID Estudiante: " + studentId));
        doc.add(new Paragraph("ID Curso: " + courseId));
        doc.add(new Paragraph("Periodo: " + (attendances.isEmpty() ? "Sin datos" :
                sdf.format(attendances.get(0).getFecha()) + " a " +
                        sdf.format(attendances.get(attendances.size() - 1).getFecha()))));
        doc.add(new Paragraph("\n"));

        // Tabla de asistencias
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 3, 6})).useAllAvailableWidth();
        table.addHeaderCell("Fecha");
        table.addHeaderCell("Tipo");
        table.addHeaderCell("Excusa");

        for (Attendance att : attendances) {
            table.addCell(sdf.format(att.getFecha()));
            table.addCell(att.getTipo());
            table.addCell(att.getExcusa() != null ? att.getExcusa() : "-");
        }

        doc.add(table);
        doc.add(new Paragraph("\n"));

        // Resumen
        Map<String, Long> resumen = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getTipo, Collectors.counting()));

        doc.add(new Paragraph("Resumen:")
                .setBold());
        for (Map.Entry<String, Long> entry : resumen.entrySet()) {
            doc.add(new Paragraph("- " + entry.getKey() + ": " + entry.getValue()));
        }

        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("_______________________________\nFirma del tutor o responsable"));

        doc.close();
        return new ByteArrayResource(out.toByteArray());
    }

    public ByteArrayResource generateCourseStatsPdf(List<Attendance> attendances, Integer courseId) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph("Estadísticas de Asistencia - Curso " + courseId)
                .setFontSize(16)
                .setBold());

        // Resumen general
        long total = attendances.size();
        long presente = attendances.stream().filter(a -> a.getTipo().equals("presente")).count();
        long ausente = attendances.stream().filter(a -> a.getTipo().equals("ausente")).count();
        long tardanza = attendances.stream().filter(a -> a.getTipo().equals("tardanza")).count();
        double tasa = total > 0 ? ((double) presente / total) * 100 : 0;

        doc.add(new Paragraph("Total registros: " + total));
        doc.add(new Paragraph("Tasa de asistencia: " + String.format("%.2f", tasa) + "%"));

        // → GRÁFICO PIE
        byte[] pieChart = ChartUtils.createPieChart(presente, ausente, tardanza);
        Image pieImage = new Image(ImageDataFactory.create(pieChart));
        doc.add(new Paragraph("Distribución de asistencias").setBold());
        doc.add(pieImage.scaleToFit(300, 300));

        // → GRÁFICO DE LÍNEAS (evolución)
        byte[] lineChart = ChartUtils.createLineChart(attendances);
        Image lineImage = new Image(ImageDataFactory.create(lineChart));
        doc.add(new Paragraph("Tendencia de asistencia diaria").setBold());
        doc.add(lineImage.scaleToFit(500, 300));

        doc.close();
        return new ByteArrayResource(out.toByteArray());
    }

}

