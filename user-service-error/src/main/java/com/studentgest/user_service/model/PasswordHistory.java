package com.studentgest.user_service.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "password_history")
public class PasswordHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "created_at")
    private Timestamp createdAt;
    
    // Constructores
    public PasswordHistory() {}
    
    public PasswordHistory(Integer userId, String passwordHash) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
    
    public PasswordHistory(Integer userId, String passwordHash, Timestamp createdAt) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }
    
    // Getters
    public Integer getId() {
        return id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    // Setters
    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());
        }
    }
    
    @Override
    public String toString() {
        return "PasswordHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", passwordHash='" + passwordHash + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}