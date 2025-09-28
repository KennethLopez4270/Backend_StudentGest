package com.studentgest.attendance_service.model;

import jakarta.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "asistencia")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_asistencia;

    @Column(name = "id_estudiante", nullable = false)
    private Integer studentId;

    @Column(name = "id_curso", nullable = false)
    private Integer courseId;

    @Column(nullable = false)
    private Date fecha;

    @Column(nullable = false)
    private String tipo; // 'presente', 'ausente', 'tardanza'

    private String excusa;

    @Column(name = "registrado_por", nullable = false)
    private Integer registeredBy;

    @Column(name = "creado_en", updatable = false)
    private Timestamp createdAt;

    // Getters y Setters
    public Integer getId_asistencia() { return id_asistencia; }
    public void setId_asistencia(Integer id_asistencia) { this.id_asistencia = id_asistencia; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getExcusa() { return excusa; }
    public void setExcusa(String excusa) { this.excusa = excusa; }
    public Integer getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(Integer registeredBy) { this.registeredBy = registeredBy; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}