package com.studentgest.user_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Entity
@Table(name = "seguridad_politicas")
@Data
public class SecurityPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "nombre_politica", nullable = false, length = 100, unique = true)
    private String nombrePolitica;
    
    @Column(name = "valor", nullable = false, length = 255)
    private String valor;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "editable")
    private Boolean editable = true;
    
    @Column(name = "categoria", length = 50)
    private String categoria;
    
    @Column(name = "creado_en")
    private Timestamp creadoEn;
    
    @Column(name = "actualizado_en")
    private Timestamp actualizadoEn;
    
    @PrePersist
    protected void onCreate() {
        creadoEn = new Timestamp(System.currentTimeMillis());
        actualizadoEn = new Timestamp(System.currentTimeMillis());
    }
    
    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = new Timestamp(System.currentTimeMillis());
    }
    
    // GETTERS Y SETTERS MANUALES (por si Lombok falla)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getNombrePolitica() { return nombrePolitica; }
    public void setNombrePolitica(String nombrePolitica) { this.nombrePolitica = nombrePolitica; }
    
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public Boolean getEditable() { return editable; }
    public void setEditable(Boolean editable) { this.editable = editable; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public Timestamp getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Timestamp creadoEn) { this.creadoEn = creadoEn; }
    
    public Timestamp getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Timestamp actualizadoEn) { this.actualizadoEn = actualizadoEn; }
    
    // Métodos helper para obtener valores tipados
    public Integer getValorComoInteger() {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public Boolean getValorComoBoolean() {
        return "true".equalsIgnoreCase(valor) || "1".equals(valor);
    }
    
    public String getValorComoString() {
        return valor;
    }
}