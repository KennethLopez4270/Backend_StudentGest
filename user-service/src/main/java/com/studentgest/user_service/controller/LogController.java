package com.studentgest.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final String LOG_DIR = "logs/";

    // Listar archivos de log disponibles
    @GetMapping("/files")
    @PreAuthorize("hasAnyRole('ADMIN', 'OSI')")
    public ResponseEntity<?> listLogFiles() {
        try (Stream<Path> paths = Files.walk(Paths.get(LOG_DIR))) {
            List<String> files = paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "files", files));
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error leyendo directorio de logs"));
        }
    }

    // Leer contenido de un archivo de log
    @GetMapping("/content")
    @PreAuthorize("hasAnyRole('ADMIN', 'OSI')")
    public ResponseEntity<?> getLogContent(@RequestParam String filename) {
        try {
            // Validación básica de seguridad para evitar Path Traversal
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Nombre de archivo inválido"));
            }

            Path path = Paths.get(LOG_DIR + filename);
            if (!Files.exists(path)) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Archivo no encontrado"));
            }

            // Leer las últimas 1000 líneas para no sobrecargar
            List<String> lines = Files.readAllLines(path);
            int start = Math.max(0, lines.size() - 1000);
            List<String> lastLines = lines.subList(start, lines.size());

            return ResponseEntity.ok(Map.of("success", true, "content", lastLines));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error leyendo archivo de log"));
        }
    }
}
