package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.model.SinistreRequest;
import com.vermeg.sinistpro.service.SinistreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sinistres")
public class SinistreController {
    private final SinistreService sinistreService;

    public SinistreController(SinistreService sinistreService) {
        this.sinistreService = sinistreService;
    }

    // Create a new claim (replaces POST /api/claims)
    @PostMapping
    public ResponseEntity<Sinistre> déclarerSinistre(@RequestBody SinistreRequest request) {
        return ResponseEntity.ok(sinistreService.créerSinistre(request));
    }

    // Get claim by ID (replaces GET /api/claims/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Sinistre> consulterStatut(@PathVariable Long id) {
        return ResponseEntity.ok(sinistreService.consulterStatut(id));
    }

    // Get all claims
    @GetMapping
    public ResponseEntity<List<Sinistre>> getSinistres() {
        return ResponseEntity.ok(sinistreService.getAllSinistres());
    }

    // Update claim status
    @PutMapping("/{id}/status")
    public ResponseEntity<Sinistre> updateStatus(@PathVariable Long id, @RequestBody ClaimStatus newStatus) {
        return ResponseEntity.ok(sinistreService.updateStatus(id, newStatus));
    }
}

    /*@Autowired
    private SinistreRepository repository;

    @PostMapping
    public Sinistre createSinistre(@RequestBody ClaimRequest request) { // Use ClaimRequest
        return service.créerSinistre(request); // This triggers priority/status logic
    }

    @PostMapping
    public Sinistre createSinistre(@RequestBody Sinistre sinistre) {
        return repository.save(sinistre);
    }

    @GetMapping
    public List<Sinistre> getSinistres() {
        return repository.findAll();
    }*/
