package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Asset;
import com.vermeg.sinistpro.service.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping("/{policyId}")
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset, @PathVariable Long policyId) {
        return ResponseEntity.ok(assetService.createAsset(asset, policyId));
    }
}