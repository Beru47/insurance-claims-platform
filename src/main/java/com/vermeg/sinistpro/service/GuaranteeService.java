package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Guarantee;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.repository.GuaranteeRepository;
import com.vermeg.sinistpro.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuaranteeService {

    private final GuaranteeRepository guaranteeRepository;
    private final PolicyRepository policyRepository;

    public GuaranteeService(GuaranteeRepository guaranteeRepository, PolicyRepository policyRepository) {
        this.guaranteeRepository = guaranteeRepository;
        this.policyRepository = policyRepository;
    }

    public Guarantee createGuarantee(Guarantee guarantee) {
        if (guaranteeRepository.existsByName(guarantee.getName())) {
            throw new IllegalArgumentException("Guarantee with name " + guarantee.getName() + " already exists");
        }
        return guaranteeRepository.save(guarantee);
    }

    public Guarantee associateGuaranteeWithPolicy(Long guaranteeId, Long policyId) {
        Guarantee guarantee = guaranteeRepository.findById(guaranteeId)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with ID: " + guaranteeId));
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policyId));

        if (!guarantee.getPolicies().contains(policy)) {
            guarantee.getPolicies().add(policy);
            guaranteeRepository.save(guarantee);
        }
        return guarantee;
    }

    public List<Guarantee> getGuaranteesByPolicyId(Long policyId) {
        return guaranteeRepository.findByPoliciesId(policyId);
    }

    public Guarantee getGuarantee(Long id) {
        return guaranteeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with ID: " + id));
    }

    public List<Guarantee> getAllGuarantees() {
        return guaranteeRepository.findAll();
    }

    public Guarantee updateGuarantee(Long id, Guarantee updatedGuarantee) {
        Guarantee existingGuarantee = getGuarantee(id);
        if (!existingGuarantee.getName().equals(updatedGuarantee.getName()) &&
                guaranteeRepository.existsByName(updatedGuarantee.getName())) {
            throw new IllegalArgumentException("Guarantee with name " + updatedGuarantee.getName() + " already exists");
        }
        existingGuarantee.setName(updatedGuarantee.getName());
        existingGuarantee.setDescription(updatedGuarantee.getDescription());
        existingGuarantee.setType(updatedGuarantee.getType());
        existingGuarantee.setCoverageAmount(updatedGuarantee.getCoverageAmount());
        return guaranteeRepository.save(existingGuarantee);
    }

    public void removeGuaranteeFromPolicy(Long guaranteeId, Long policyId) {
        Guarantee guarantee = guaranteeRepository.findById(guaranteeId)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with ID: " + guaranteeId));
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policyId));

        guarantee.getPolicies().remove(policy);
        guaranteeRepository.save(guarantee);
    }
}