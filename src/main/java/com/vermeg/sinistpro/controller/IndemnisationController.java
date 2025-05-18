/*package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.service.IndemnificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indemnisations")
public class IndemnisationController {
    private final IndemnificationService indemnificationService;

    public IndemnisationController(IndemnificationService indemnificationService) {
        this.indemnificationService = indemnificationService;
    }

    @PostMapping("/calculate/{sinistreId}")
    public ResponseEntity<Sinistre> calculateIndemnity(@PathVariable Long sinistreId) {
        Sinistre sinistre = indemnificationService.calculateIndemnity(sinistreId);
        return ResponseEntity.ok(sinistre);
    }
}*/