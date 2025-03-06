package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.event.ClaimEventPublisher;
import com.vermeg.sinistpro.exception.SinistreException;
import com.vermeg.sinistpro.model.*;
import com.vermeg.sinistpro.repository.SinistreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SinistreService {

    private final SinistreRepository repo;
    private final ClaimEventPublisher publisher;

    @Autowired
    private ExpertService expertService;  // Inject ExpertService to manage Experts

    @Autowired
    private AssureService assureService; // Inject AssureService to manage Assures

    public SinistreService(SinistreRepository repo, ClaimEventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    // Method to create a new claim (Sinistre)
    public Sinistre créerSinistre(SinistreRequest request) {
        int priority = calculerPriorityScore(request);  // Score calculation
        Assure assure = assureService.findAssureByNumeroPolice(request.getNumeroPolice());  // Find Assure by policy number
        Sinistre sinistre = new Sinistre(request.getType(), request.getDate(), request.getLieu(), request.getDescription(), ClaimStatus.PENDING, priority, assure);
        Sinistre saved = repo.save(sinistre);
        publisher.publierÉvénement(saved);  // Publish event to Kafka
        return saved;
    }

    // Method to calculate priority score based on request type and location
    private int calculerPriorityScore(SinistreRequest request) {
        int score = 0;
        if ("Accident".equalsIgnoreCase(request.getType())) score += 50;
        if ("Incendie".equalsIgnoreCase(request.getType())) score += 70;
        if (request.getLieu() != null && request.getLieu().equalsIgnoreCase("Zone dangereuse")) score += 30;
        return Math.min(score, 100); // To avoid score exceeding 100
    }

    // Method to assign an expert to the Sinistre
    public void attribuerExpert(Long sinistreId, Long expertId) {
        Sinistre sinistre = repo.findById(sinistreId).orElseThrow(() -> new SinistreException("Sinistre introuvable"));
        Expert expert = expertService.findExpertById(expertId);
        sinistre.setExpert(expert); // Assign expert
        repo.save(sinistre);
    }

    // Method to consult the status of a Sinistre
    public Sinistre consulterStatut(Long id) {
        return repo.findById(id).orElseThrow(() -> new SinistreException("Sinistre introuvable"));
    }

    // Method to assign an Assure to the Sinistre using the policy number
    public Sinistre assignerAssure(Long sinistreId, String numeroPolice) {
        Sinistre sinistre = repo.findById(sinistreId).orElseThrow(() -> new SinistreException("Sinistre introuvable"));
        Assure assure = assureService.findAssureByNumeroPolice(numeroPolice);
        sinistre.setAssure(assure); // Assign Assure
        return repo.save(sinistre);
    }

    public Sinistre findSinistreById(Long id) {
        return repo.findById(id).orElseThrow(() -> new SinistreException("Sinistre not found"));
    }

    public Sinistre assignAssureToSinistre(Long sinistreId, String numeroPolice) {
        Sinistre sinistre = findSinistreById(sinistreId);  // Using the find method to get the Sinistre by ID
        Assure assure = assureService.findAssureByNumeroPolice(numeroPolice);  // Using AssureService to find Assure by policy number
        sinistre.setAssure(assure);  // Assign the Assure to the Sinistre
        return repo.save(sinistre);  // Save the updated Sinistre with the Assure assigned
    }

    // Add this method to SinistreService
    public List<Sinistre> findAllSinistres() {
        return repo.findAll();  // This retrieves all Sinistre records from the repository
    }


}
