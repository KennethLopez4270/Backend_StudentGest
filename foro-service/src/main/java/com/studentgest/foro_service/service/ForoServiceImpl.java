package com.studentgest.foro_service.service;

import com.studentgest.foro_service.dto.*;
import com.studentgest.foro_service.exception.ForoNotFoundException;
import com.studentgest.foro_service.model.Foro;
import com.studentgest.foro_service.model.MensajeForo;
import com.studentgest.foro_service.repository.ForoRepository;
import com.studentgest.foro_service.repository.MensajeForoRepository;
import com.studentgest.foro_service.service.ForoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForoServiceImpl implements ForoService {

    private final ForoRepository foroRepository;
    private final MensajeForoRepository mensajeForoRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ForoServiceImpl(ForoRepository foroRepository,
                           MensajeForoRepository mensajeForoRepository,
                           ModelMapper modelMapper) {
        this.foroRepository = foroRepository;
        this.mensajeForoRepository = mensajeForoRepository;
        this.modelMapper = modelMapper;
        configureMappings();
    }

    private void configureMappings() {
        // Configuración específica para evitar problemas con IDs y relaciones
        modelMapper.createTypeMap(ForoRequest.class, Foro.class)
                .addMappings(mapper -> {
                    mapper.skip(Foro::setId);
                    mapper.skip(Foro::setMensajes);
                });

        modelMapper.createTypeMap(MensajeRequest.class, MensajeForo.class)
                .addMappings(mapper -> {
                    mapper.skip(MensajeForo::setId);
                    mapper.skip(MensajeForo::setFecha);
                });
    }

    @Override
    public ForoResponse crearForo(ForoRequest request) {
        Foro foro = modelMapper.map(request, Foro.class);
        foro.setCreadoEn(LocalDateTime.now());

        Foro foroGuardado = foroRepository.save(foro);
        return modelMapper.map(foroGuardado, ForoResponse.class);
    }

    @Override
    public ForoResponse agregarMensaje(Long foroId, MensajeRequest request) {
        Foro foro = foroRepository.findById(foroId)
                .orElseThrow(() -> new RuntimeException("Foro no encontrado"));

        MensajeForo mensaje = modelMapper.map(request, MensajeForo.class);
        mensaje.setForo(foro);
        mensaje.setFecha(LocalDateTime.now());

        mensajeForoRepository.save(mensaje);

        return modelMapper.map(foro, ForoResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForoResponse> obtenerTodosLosForos() {
        return foroRepository.findAll().stream()
                .map(foro -> modelMapper.map(foro, ForoResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ForoResponse obtenerForoPorId(Long id) {
        Foro foro = foroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foro no encontrado"));
        return modelMapper.map(foro, ForoResponse.class);
    }

    @Override
    public ForoResponse actualizarForo(Long id, ForoRequest request) {
        Foro foro = foroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foro no encontrado"));

        modelMapper.map(request, foro);
        Foro foroActualizado = foroRepository.save(foro);

        return modelMapper.map(foroActualizado, ForoResponse.class);
    }

    @Override
    public void eliminarForo(Long id) {
        Foro foro = foroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foro no encontrado"));
        foroRepository.delete(foro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForoResponse> obtenerForosPorCurso(Long cursoId) {
        return foroRepository.findByCursoId(cursoId).stream()
                .map(foro -> {
                    ForoResponse response = modelMapper.map(foro, ForoResponse.class);
                    // Puedes agregar lógica adicional aquí si es necesario
                    return response;
                })
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<MensajeResponse> obtenerMensajesPorForo(Long foroId) {
        // Verificar que el foro existe
        if (!foroRepository.existsById(foroId)) {
            throw new ForoNotFoundException(foroId);
        }

        return mensajeForoRepository.findByForoIdOrderByFechaDesc(foroId).stream()
                .map(this::convertirAMensajeResponse)
                .collect(Collectors.toList());
    }

    private MensajeResponse convertirAMensajeResponse(MensajeForo mensaje) {
        MensajeResponse response = new MensajeResponse();
        response.setId(mensaje.getId());
        response.setForoId(mensaje.getForo().getId());
        response.setUsuarioId(mensaje.getUsuarioId());
        response.setMensaje(mensaje.getMensaje());
        response.setFecha(mensaje.getFecha());
        return response;
    }
}