package com.studentgest.user_service.repository;

import com.studentgest.user_service.model.InformationAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InformationAssetRepository extends JpaRepository<InformationAsset, Long> {
    List<InformationAsset> findByOwner(String owner);
}
