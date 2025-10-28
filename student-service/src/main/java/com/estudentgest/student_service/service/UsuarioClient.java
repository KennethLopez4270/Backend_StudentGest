package com.estudentgest.student_service.service;

import com.estudentgest.student_service.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UsuarioClient {

    @Value("${user-service.base-url}")
    private String userServiceBaseUrl;

    private final RestTemplate restTemplate;

    public UsuarioClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UsuarioDTO getUsuarioById(Long idUsuario) {
        try {
            String url = userServiceBaseUrl + "/" + idUsuario;
            System.out.println("LLAMANDO A: " + url); // DEBUG
            UsuarioDTO response = restTemplate.getForObject(url, UsuarioDTO.class);
            System.out.println("RESPUESTA: " + response); // DEBUG
            return response;
        } catch (Exception e) {
            System.err.println("ERROR AL LLAMAR USER-SERVICE: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}