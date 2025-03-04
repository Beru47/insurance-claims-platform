package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.event.ClaimEventPublisher;
import com.vermeg.sinistpro.exception.SinistreException;
import com.vermeg.sinistpro.model.ClaimRequest;
import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.repository.SinistreRepository;
import org.springframework.stereotype.Service;

@Service
public class SinistreService {
    private final SinistreRepository repo;
    private final ClaimEventPublisher publisher;

    public SinistreService(SinistreRepository repo, ClaimEventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    public Sinistre créerSinistre(ClaimRequest request) {
        int priority = calculerPriorityScore(request);  // Score calculation
        Sinistre sinistre = new Sinistre(null, request.getType(), request.getDate(), request.getLieu(), request.getDescription(), ClaimStatus.PENDING, priority);
        Sinistre saved = repo.save(sinistre);
        publisher.publierÉvénement(saved);  // Publish event to Kafka
        return saved;
    }

    private int calculerPriorityScore(ClaimRequest request) {
        int score = 0;
        if ("Accident".equalsIgnoreCase(request.getType())) score += 50;
        if ("Incendie".equalsIgnoreCase(request.getType())) score += 70;
        if (request.getLieu() != null && request.getLieu().equalsIgnoreCase("Zone dangereuse")) score += 30;
        return Math.min(score, 100); // zedt != null to solve NullPointerException
    }


    public Sinistre consulterStatut(Long id) {
        return repo.findById(id).orElseThrow(() -> new SinistreException("Sinistre introuvable"));
    }
}
