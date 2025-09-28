package com.homework.homework_service.service;

import com.homework.homework_service.client.CursoMateriaProfesorClient;
import com.homework.homework_service.client.EstudiantesPorCursoMateriaClient;
import com.homework.homework_service.dto.CursoMateriaEstudiantesDTO;
import com.homework.homework_service.dto.CursoMateriaProfesorDTO;
import com.homework.homework_service.dto.TareaDTO;
import com.homework.homework_service.model.EntregaTarea;
import com.homework.homework_service.model.Tarea;
import com.homework.homework_service.repository.EntregaTareaRepository;
import com.homework.homework_service.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;
    private final CursoMateriaProfesorClient cursoMateriaProfesorClient;

    @Autowired
    private EstudiantesPorCursoMateriaClient estudiantesClient;

    @Autowired
    public TareaService(TareaRepository tareaRepository, CursoMateriaProfesorClient cursoMateriaProfesorClient) {
        this.tareaRepository = tareaRepository;
        this.cursoMateriaProfesorClient = cursoMateriaProfesorClient;
    }

    @Autowired
    private EntregaTareaRepository entregaTareaRepository;

    public List<Tarea> listarTareas() {
        return tareaRepository.findAll();
    }

    public Tarea obtenerTarea(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public Tarea crearTarea(TareaDTO dto) {
        // Obtener datos del CMP
        CursoMateriaProfesorDTO cmp = cursoMateriaProfesorClient.getCursoMateriaProfesorById(dto.getIdCmp().longValue());

        // Crear y guardar la tarea
        Tarea tarea = Tarea.builder()
                .idCmp(dto.getIdCmp())
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .fechaEntrega(dto.getFechaEntrega())
                .creadoPor(cmp.getIdProfesor().intValue())
                .creadoEn(LocalDate.now())
                .build();
        Tarea tareaGuardada = tareaRepository.save(tarea);

        // Obtener estudiantes de ese CMP
        CursoMateriaEstudiantesDTO estudiantesDTO = estudiantesClient.getEstudiantesPorCmp(dto.getIdCmp().longValue());

        // Crear entregas en estado "pendiente"
        List<EntregaTarea> entregas = estudiantesDTO.getEstudiantes().stream()
                .map(idEst -> EntregaTarea.builder()
                        .idTarea(tareaGuardada.getIdTarea())
                        .idEstudiante(idEst)
                        .estado("pendiente")
                        .build())
                .toList();

        entregaTareaRepository.saveAll(entregas);

        return tareaGuardada;
    }

    public Tarea actualizarTarea(Long id, TareaDTO dto) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        CursoMateriaProfesorDTO cmp = cursoMateriaProfesorClient.getCursoMateriaProfesorById(dto.getIdCmp().longValue());

        tarea.setIdCmp(dto.getIdCmp());
        tarea.setTitulo(dto.getTitulo());
        tarea.setDescripcion(dto.getDescripcion());
        tarea.setFechaEntrega(dto.getFechaEntrega());
        tarea.setCreadoPor(cmp.getIdProfesor().intValue());

        return tareaRepository.save(tarea);
    }

    public void eliminarTarea(Long id) {
        tareaRepository.deleteById(id);
    }
}
