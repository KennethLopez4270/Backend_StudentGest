package com.homework.homework_service.client;

import com.homework.homework_service.dto.CursoMateriaProfesorDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CursoMateriaProfesorClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CursoMateriaProfesorClient(RestTemplate restTemplate,
                                      @Value("${student-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public CursoMateriaProfesorDTO getCursoMateriaProfesorById(Long idCmp) {
        String url = baseUrl + "/curso_materia_profesor/" + idCmp;
        return restTemplate.getForObject(url, CursoMateriaProfesorDTO.class);
    }
}
