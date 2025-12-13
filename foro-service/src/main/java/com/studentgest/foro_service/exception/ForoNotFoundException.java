package com.studentgest.foro_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ForoNotFoundException extends RuntimeException {

    public ForoNotFoundException(Long foroId) {
        super("No se encontró el foro con ID: " + foroId);
    }

    public ForoNotFoundException(String mensaje) {
        super(mensaje);
    }
}