package com.studentgest.attendance_service.report;

import com.studentgest.attendance_service.model.Attendance;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ChartUtils {

    public static byte[] createPieChart(long presente, long ausente, long tardanza) throws Exception {
        PieChart chart = new PieChartBuilder().width(400).height(300).title("Distribución por tipo").build();
        chart.addSeries("Presente", presente);
        chart.addSeries("Ausente", ausente);
        chart.addSeries("Tardanza", tardanza);
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setChartFontColor(Color.DARK_GRAY);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);
        return baos.toByteArray();
    }

    public static byte[] createLineChart(List<Attendance> attendances) throws Exception {
        Map<String, Long> porFecha = attendances.stream()
                .filter(a -> a.getTipo().equals("presente"))
                .collect(Collectors.groupingBy(
                        a -> new SimpleDateFormat("yyyy-MM-dd").format(a.getFecha()),
                        TreeMap::new,
                        Collectors.counting()
                ));

        XYChart chart = new XYChartBuilder().width(500).height(300).title("Presencias por día").xAxisTitle("Fecha").yAxisTitle("Cantidad").build();
        chart.getStyler().setDatePattern("yyyy-MM-dd");
        chart.getStyler().setLegendVisible(false);

        List<Date> fechas = porFecha.keySet().stream().map(f -> {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(f);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        List<Long> valores = new ArrayList<>(porFecha.values());

        chart.addSeries("Presente", fechas, valores);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);
        return baos.toByteArray();
    }
}

