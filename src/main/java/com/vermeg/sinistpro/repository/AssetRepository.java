package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
}