package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.model.SinistreRequest;
import com.vermeg.sinistpro.service.SinistreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sinistres")
public class AssureController {
    private final SinistreService sinistreService;

    public AssureController(SinistreService sinistreService) {
        this.sinistreService = sinistreService;
    }

    @PostMapping
    public ResponseEntity<Sinistre> déclarerSinistre(@RequestBody SinistreRequest request) {
        return ResponseEntity.ok(sinistreService.créerSinistre(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sinistre> consulterStatut(@PathVariable Long id) {
        return ResponseEntity.ok(sinistreService.consulterStatut(id));
    }
}

/*@RestController
@RequestMapping("/assures")
public class AssureController {
    private final AssureService assureService;

    public AssureController(AssureService assureService) {
        this.assureService = assureService;
    }

    @PostMapping("/{id}/sinistres")
    public ResponseEntity<Sinistre> declareSinistre(@PathVariable Long id, @RequestBody ClaimRequest claimRequest) {
        Sinistre sinistre = assureService.declarerSinistre(id, claimRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(sinistre);
    }

    @GetMapping("/{id}/sinistres/{sinistreId}")
    public ResponseEntity<ClaimStatus> checkStatus(@PathVariable Long id, @PathVariable Long sinistreId) {
        ClaimStatus status = assureService.consulterStatut(id, sinistreId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{id}/historique")
    public ResponseEntity<List<Sinistre>> getHistory(@PathVariable Long id) {
        List<Sinistre> sinistres = assureService.consulterHistorique(id);
        return ResponseEntity.ok(sinistres);
    }
}
*/