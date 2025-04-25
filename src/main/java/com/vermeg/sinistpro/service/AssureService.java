/*package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Assure;
import com.vermeg.sinistpro.repository.AssureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AssureService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int POLICY_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();
    @Autowired
    private AssureRepository assureRepository;


    public AssureService(AssureRepository assureRepository) {
        this.assureRepository = assureRepository;
    }

    public Assure createAssure(Assure assure) {
        // Generate unique numeroPolice if not provided
        if (assure.getNumeroPolice() == null || assure.getNumeroPolice().isEmpty()) {
            assure.setNumeroPolice(generateNumeroPolice());
        }
        // Verify uniqueness
        if (assureRepository.findByNumeroPolice(assure.getNumeroPolice()).isPresent()) {
            assure.setNumeroPolice(generateNumeroPolice());
        }
        return assureRepository.save(assure);
    }

    private String generateNumeroPolice() {
        StringBuilder policy = new StringBuilder(POLICY_LENGTH);
        for (int i = 0; i < POLICY_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            policy.append(CHARACTERS.charAt(index));
        }
        return policy.toString();
    }
}

 */
