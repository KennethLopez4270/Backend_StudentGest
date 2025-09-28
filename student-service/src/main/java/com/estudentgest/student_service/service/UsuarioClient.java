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

    @Autowired
    private RestTemplate restTemplate;

    public UsuarioDTO getUsuarioById(Long idUsuario) {
        try {
            return restTemplate.getForObject(userServiceBaseUrl + "/" + idUsuario, UsuarioDTO.class);
        } catch (Exception e) {
            return null; // luego cambiar a loguear el error
        }
    }
}

