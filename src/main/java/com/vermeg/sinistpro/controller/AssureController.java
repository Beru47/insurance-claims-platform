package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.ClaimRequest;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.service.SinistreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
public class AssureController {
    private final SinistreService sinistreService;

    public AssureController(SinistreService sinistreService) {
        this.sinistreService = sinistreService;
    }

    @PostMapping
    public ResponseEntity<Sinistre> déclarerSinistre(@RequestBody ClaimRequest request) {
        return ResponseEntity.ok(sinistreService.créerSinistre(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sinistre> consulterStatut(@PathVariable Long id) {
        return ResponseEntity.ok(sinistreService.consulterStatut(id));
    }
}
