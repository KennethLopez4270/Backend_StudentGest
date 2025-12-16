package com.studentgest.user_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "information_assets")
public class InformationAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String systemName;
    private String description;
    private String owner;
    private String authorizer;
    private String status;
    private String comments;

    // CIDP Values (1, 2, 3)
    private int confidentialityVal;
    private int integrityVal;
    private int availabilityVal;

    // Privacy
    private boolean hasPrivacyData;
    private int privacyVal; // 0 if hasPrivacyData is false

    // Calculated fields (stored for easier querying)
    private int classificationTotal; // Percentage (0-100)
    private String classificationLevel; // "USO PÚBLICO O GENERAL", etc.
}
