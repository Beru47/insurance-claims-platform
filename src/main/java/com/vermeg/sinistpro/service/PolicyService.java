package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Client;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.repository.ClientRepository;
import com.vermeg.sinistpro.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

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
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        policy.setClient(client);
        policy.setNumeroPolice(generateNumeroPolice());
        policy.setValid(true);
        return policyRepository.save(policy);
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