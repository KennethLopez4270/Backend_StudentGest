package com.studentgest.user_service.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoUsuarioConverter implements AttributeConverter<EstadoUsuario, String> {

    @Override
    public String convertToDatabaseColumn(EstadoUsuario estado) {
        if (estado == null) return null;
        return estado.name().toLowerCase(); // convierte PENDIENTE → "pendiente"
    }

    @Override
    public EstadoUsuario convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return EstadoUsuario.valueOf(dbData.toUpperCase()); // "pendiente" → PENDIENTE
    }
}
