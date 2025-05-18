package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.Asset;
import com.vermeg.sinistpro.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByPolicy(Policy policy);
}