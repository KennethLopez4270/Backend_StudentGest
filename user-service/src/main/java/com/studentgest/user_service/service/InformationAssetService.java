package com.studentgest.user_service.service;

import com.studentgest.user_service.model.InformationAsset;
import com.studentgest.user_service.repository.InformationAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InformationAssetService {

    @Autowired
    private InformationAssetRepository repository;

    public List<InformationAsset> getAllAssets() {
        return repository.findAll();
    }

    public InformationAsset saveAsset(InformationAsset asset) {
        calculateClassification(asset);
        return repository.save(asset);
    }

    public void deleteAsset(Long id) {
        repository.deleteById(id);
    }

    private void calculateClassification(InformationAsset asset) {
        // Logic mirrored from cidp.js

        // 1. Calculate Percentages
        int percentageC = getPercentageFromValue(asset.getConfidentialityVal());
        int percentageI = getPercentageFromValue(asset.getIntegrityVal());
        int percentageD = getPercentageFromValue(asset.getAvailabilityVal());

        int percentageP = 0;
        if (asset.isHasPrivacyData()) {
            percentageP = getPrivacyPercentage(asset.getPrivacyVal());
            // Add Privacy impact to Confidentiality
            percentageC += percentageP;
        }

        // 2. Sum Total
        int total = percentageC + percentageI + percentageD;
        asset.setClassificationTotal(total);

        // 3. Determine Level
        if (total <= 40) {
            asset.setClassificationLevel("USO PÚBLICO O GENERAL");
        } else if (total <= 60) {
            asset.setClassificationLevel("USO INTERNO O PRIVADO");
        } else {
            asset.setClassificationLevel("CRÍTICO O CONFIDENCIAL");
        }
    }

    private int getPercentageFromValue(int val) {
        return switch (val) {
            case 3 -> 30;
            case 2 -> 20;
            case 1 -> 10;
            default -> 0;
        };
    }

    private int getPrivacyPercentage(int val) {
        return switch (val) {
            case 3 -> 10;
            case 2 -> 5;
            case 1 -> 1;
            default -> 0;
        };
    }
}
