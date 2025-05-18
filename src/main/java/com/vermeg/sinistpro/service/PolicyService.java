package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.exception.GlobalExceptionHandler;
import com.vermeg.sinistpro.exception.ResourceNotFoundException;
import com.vermeg.sinistpro.model.Asset;
import com.vermeg.sinistpro.model.Client;
import com.vermeg.sinistpro.model.Guarantee;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.repository.ClientRepository;
import com.vermeg.sinistpro.repository.GuaranteeRepository;
import com.vermeg.sinistpro.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class PolicyService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int POLICY_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();
    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;
    private final GuaranteeRepository guaranteeRepository;

    public PolicyService(PolicyRepository policyRepository, ClientRepository clientRepository, GuaranteeRepository guaranteeRepository) {
        this.policyRepository = policyRepository;
        this.clientRepository = clientRepository;
        this.guaranteeRepository = guaranteeRepository;
    }

    public Policy createPolicy(Policy policy, Long clientId, Asset asset) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new GlobalExceptionHandler("Client not found with id: " + clientId));
        policy.setClient(client);
        policy.setNumeroPolice(generateNumeroPolice());
        policy.setValid(true);
        // Resolve Guarantee entities
        List<Guarantee> resolvedGuarantees = new ArrayList<>();
        for (Guarantee guarantee : policy.getGuarantees()) {
            Guarantee existing = guaranteeRepository.findByName(guarantee.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Guarantee not found: " + guarantee.getName()));
            resolvedGuarantees.add(existing);
        }
        policy.setGuarantees(resolvedGuarantees);
        if (asset != null) {
            asset.setPolicy(policy);
            policy.setAsset(asset);
        }
        return policyRepository.save(policy);
    }

    public Policy getPolicy(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler("Policy not found with id: " + id));
    }

    public List<Policy> getPoliciesByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        return policyRepository.findByClient(client);
    }

    private String generateNumeroPolice() {
        StringBuilder policy = new StringBuilder(POLICY_LENGTH);
        for (int i = 0; i < POLICY_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            policy.append(CHARACTERS.charAt(index));
        }
        String result = policy.toString();
        int maxRetries = 3;
        int attempts = 0;
        while (policyRepository.findByNumeroPolice(result).isPresent() && attempts < maxRetries) {
            policy.setLength(0);
            for (int i = 0; i < POLICY_LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                policy.append(CHARACTERS.charAt(index));
            }
            result = policy.toString();
            attempts++;
        }
        if (attempts >= maxRetries) {
            throw new IllegalStateException("Unable to generate unique numeroPolice");
        }
        return result;
    }
}


/*package com.vermeg.sinistpro.service;


import com.vermeg.sinistpro.exception.GlobalExceptionHandler;
import com.vermeg.sinistpro.exception.ResourceNotFoundException;
import com.vermeg.sinistpro.model.Client;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.repository.ClientRepository;
import com.vermeg.sinistpro.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class PolicyService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int POLICY_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();
    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;

    public PolicyService(PolicyRepository policyRepository, ClientRepository clientRepository) {
        this.policyRepository = policyRepository;
        this.clientRepository = clientRepository;
    }

    public Policy createPolicy(Policy policy, Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new GlobalExceptionHandler("Client not found with id: " + clientId));
        policy.setClient(client);
        policy.setNumeroPolice(generateNumeroPolice());
        policy.setValid(true);
        return policyRepository.save(policy);
    }

    public Policy getPolicy(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler("Policy not found with id: " + id));
    }

    public List<Policy> getPoliciesByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        return policyRepository.findByClient(client);
    }

    private String generateNumeroPolice() {
        StringBuilder policy = new StringBuilder(POLICY_LENGTH);
        for (int i = 0; i < POLICY_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            policy.append(CHARACTERS.charAt(index));
        }
        String result = policy.toString();
        int maxRetries = 3;
        int attempts = 0;
        while (policyRepository.findByNumeroPolice(result).isPresent() && attempts < maxRetries) {
            policy.setLength(0);
            for (int i = 0; i < POLICY_LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                policy.append(CHARACTERS.charAt(index));
            }
            result = policy.toString();
            attempts++;
        }
        if (attempts >= maxRetries) {
            throw new IllegalStateException("Unable to generate unique numeroPolice");
        }
        return result;
    }


}*/
