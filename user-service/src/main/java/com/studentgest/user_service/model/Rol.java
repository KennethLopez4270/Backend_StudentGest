package com.studentgest.user_service.model;

public enum Rol {
    PROFESOR,
    PERSONAL,
    PADRE,
    ESTUDIANTE,
    DIRECTOR;

    @Override
    public String toString() {
        return name().toLowerCase(); // Esto devuelve "profesor", "personal", etc.
    }
}

