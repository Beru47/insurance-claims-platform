package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.service.PolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/{clientId}")
    public ResponseEntity<Policy> createPolicy(@RequestBody Policy policy, @PathVariable Long clientId) {
        return ResponseEntity.ok(policyService.createPolicy(policy, clientId));
    }
}