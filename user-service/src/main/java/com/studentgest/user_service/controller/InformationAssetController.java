package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.InformationAsset;
import com.studentgest.user_service.service.InformationAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class InformationAssetController {

    @Autowired
    private InformationAssetService service;

    @GetMapping
    public List<InformationAsset> getAllAssets() {
        return service.getAllAssets();
    }

    @PostMapping
    public InformationAsset createOrUpdateAsset(@RequestBody InformationAsset asset) {
        return service.saveAsset(asset);
    }

    @DeleteMapping("/{id}")
    public void deleteAsset(@PathVariable Long id) {
        service.deleteAsset(id);
    }
}
