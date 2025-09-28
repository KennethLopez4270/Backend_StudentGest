package com.studentgest.attendance_service.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "historialasistencias")
public class AttendanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_historial;

    @Column(name = "id_asistencia", nullable = false)
    private Integer attendanceId;

    private String tipo_anterior;
    private String tipo_nuevo;
    private String excusa_anterior;
    private String excusa_nueva;

    @Column(name = "modificado_por", nullable = false)
    private Integer modifiedBy;

    @Column(name = "fecha_modificacion", updatable = false)
    private Timestamp modifiedAt;

    // Getters y Setters
    public Integer getId_historial() { return id_historial; }
    public void setId_historial(Integer id_historial) { this.id_historial = id_historial; }
    public Integer getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Integer attendanceId) { this.attendanceId = attendanceId; }
    public String getTipo_anterior() { return tipo_anterior; }
    public void setTipo_anterior(String tipo_anterior) { this.tipo_anterior = tipo_anterior; }
    public String getTipo_nuevo() { return tipo_nuevo; }
    public void setTipo_nuevo(String tipo_nuevo) { this.tipo_nuevo = tipo_nuevo; }
    public String getExcusa_anterior() { return excusa_anterior; }
    public void setExcusa_anterior(String excusa_anterior) { this.excusa_anterior = excusa_anterior; }
    public String getExcusa_nueva() { return excusa_nueva; }
    public void setExcusa_nueva(String excusa_nueva) { this.excusa_nueva = excusa_nueva; }
    public Integer getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(Integer modifiedBy) { this.modifiedBy = modifiedBy; }
    public Timestamp getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(Timestamp modifiedAt) { this.modifiedAt = modifiedAt; }
}