package com.studentgest.user_service.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolConverter implements AttributeConverter<Rol, String> {
    @Override
    public String convertToDatabaseColumn(Rol rol) {
        return rol == null ? null : rol.name().toLowerCase();
    }

    @Override
    public Rol convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Rol.valueOf(dbData.toUpperCase());
    }
}

