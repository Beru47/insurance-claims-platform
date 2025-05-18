package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Asset;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    private static final Logger logger = LoggerFactory.getLogger(PolicyController.class);
    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping(value = "/{clientId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Policy> createPolicy(
            @RequestBody PolicyRequest policyRequest,
            @PathVariable Long clientId) {
        Policy policy = policyRequest.getPolicy();
        Asset asset = policyRequest.getAsset();
        return ResponseEntity.ok(policyService.createPolicy(policy, clientId, asset));
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<Policy> getPolicy(@PathVariable Long policyId) {
        return ResponseEntity.ok(policyService.getPolicy(policyId));
    }

    @GetMapping("/client/{numeroClient}")
    public ResponseEntity<List<Policy>> getPoliciesForLoggedInClient(@PathVariable Long numeroClient) {
        try {
            List<Policy> policies = policyService.getPoliciesByClientId(numeroClient);
            return policies.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(policies);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

/*package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    private static final Logger logger = LoggerFactory.getLogger(PolicyController.class);
    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/{clientId}")
    public ResponseEntity<Policy> createPolicy(@RequestBody Policy policy, @PathVariable Long clientId) {
        return ResponseEntity.ok(policyService.createPolicy(policy, clientId));
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<Policy> getPolicy(@PathVariable Long policyId) {
        return ResponseEntity.ok(policyService.getPolicy(policyId));
    }

    @GetMapping("/client/{numeroClient}")
    public ResponseEntity<List<Policy>> getPoliciesForLoggedInClient(@PathVariable Long numeroClient) {
        try {
            List<Policy> policies = policyService.getPoliciesByClientId(numeroClient);
            return policies.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(policies);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
*/