package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Asset;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.repository.AssetRepository;
import com.vermeg.sinistpro.repository.PolicyRepository;
import org.springframework.stereotype.Service;

@Service
public class AssetService {
    private final AssetRepository assetRepository;
    private final PolicyRepository policyRepository;

    public AssetService(AssetRepository assetRepository, PolicyRepository policyRepository) {
        this.assetRepository = assetRepository;
        this.policyRepository = policyRepository;
    }

    public Asset createAsset(Asset asset, Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        asset.setPolicy(policy);
        policy.setAsset(asset);
        Asset savedAsset = assetRepository.save(asset);
        policyRepository.save(policy);
        return savedAsset;
    }
}



/*package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Asset;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.repository.AssetRepository;
import com.vermeg.sinistpro.repository.PolicyRepository;
import org.springframework.stereotype.Service;

@Service
public class AssetService {
    private final AssetRepository assetRepository;
    private final PolicyRepository policyRepository;

    public AssetService(AssetRepository assetRepository, PolicyRepository policyRepository) {
        this.assetRepository = assetRepository;
        this.policyRepository = policyRepository;
    }

    public Asset createAsset(Asset asset, Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        asset.setPolicy(policy);
        return assetRepository.save(asset);
    }
}*/