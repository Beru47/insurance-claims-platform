package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Expert;
import com.vermeg.sinistpro.repository.ExpertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpertService {

    private final ExpertRepository expertRepository;

    @Autowired
    public ExpertService(ExpertRepository expertRepository) {
        this.expertRepository = expertRepository;
    }

    // Method to find an expert by their ID
    public Expert findExpertById(Long expertId) {
        return expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("Expert not found with id: " + expertId));
    }

    public Expert saveExpert(Expert expert) {
        return expertRepository.save(expert);
    }
}
