package com.homework.homework_service.client;

import com.homework.homework_service.dto.CursoMateriaEstudiantesDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EstudiantesPorCursoMateriaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public EstudiantesPorCursoMateriaClient(RestTemplate restTemplate,
                                            @Value("${student-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public CursoMateriaEstudiantesDTO getEstudiantesPorCmp(Long idCmp) {
        String url = baseUrl + "/curso_materia_profesor/" + idCmp + "/estudiantes";
        return restTemplate.getForObject(url, CursoMateriaEstudiantesDTO.class);
    }
}
