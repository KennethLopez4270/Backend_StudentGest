package com.studentgest.notificaciones.dto;

import java.util.List;

public class NotificacionAsignacionDTO {
    private String titulo;
    private String mensaje;
    private String canal;
    private List<Long> idsUsuarios;
    private boolean esRecurrente;
    private String frecuencia;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public List<Long> getIdsUsuarios() { return idsUsuarios; }
    public void setIdsUsuarios(List<Long> idsUsuarios) { this.idsUsuarios = idsUsuarios; }
    public boolean isEsRecurrente() { return esRecurrente; }
    public void setEsRecurrente(boolean esRecurrente) { this.esRecurrente = esRecurrente; }

    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }
}

