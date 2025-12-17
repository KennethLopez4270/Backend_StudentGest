package com.studentgest.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*") // Permitir CORS explícitamente
public class LogController {

    private final String LOG_DIR = "logs/";

    // Listar archivos de log disponibles
    @GetMapping("/files")
    public ResponseEntity<?> listLogFiles() {
        try {
            Path logPath = Paths.get(LOG_DIR);
            System.out.println("📂 Buscando logs en: " + logPath.toAbsolutePath());

            if (!Files.exists(logPath)) {
                System.out.println("❌ El directorio de logs no existe");
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "files", new ArrayList<>(),
                        "message", "Directorio de logs no encontrado: " + logPath.toAbsolutePath()));
            }

            try (Stream<Path> paths = Files.walk(logPath)) {
                List<String> files = paths
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());

                System.out.println("✅ Archivos encontrados: " + files);
                return ResponseEntity.ok(Map.of("success", true, "files", files));
            }
        } catch (IOException e) {
            System.err.println("❌ Error leyendo directorio de logs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error leyendo directorio de logs: " + e.getMessage()));
        }
    }

    // Leer contenido de un archivo de log
    @GetMapping("/content")
    public ResponseEntity<?> getLogContent(@RequestParam String filename) {
        try {
            System.out.println("📄 Solicitado archivo de log: " + filename);

            // Validación básica de seguridad para evitar Path Traversal
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Nombre de archivo inválido"));
            }

            Path path = Paths.get(LOG_DIR + filename);
            System.out.println("📂 Ruta completa: " + path.toAbsolutePath());

            if (!Files.exists(path)) {
                System.out.println("❌ Archivo no encontrado: " + path.toAbsolutePath());
                // Retornar lista vacía en lugar de 404 para no mostrar error
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "content", new ArrayList<>(),
                        "message", "Archivo no encontrado (puede estar vacío)"));
            }

            // Leer las últimas 1000 líneas para no sobrecargar
            List<String> lines = Files.readAllLines(path);
            int start = Math.max(0, lines.size() - 1000);
            List<String> lastLines = lines.subList(start, lines.size());

            System.out.println("✅ Leídas " + lastLines.size() + " líneas del archivo " + filename);
            return ResponseEntity.ok(Map.of("success", true, "content", lastLines));

        } catch (IOException e) {
            System.err.println("❌ Error leyendo archivo de log: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error leyendo archivo de log: " + e.getMessage()));
        }
    }
}
