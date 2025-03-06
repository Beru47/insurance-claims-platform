/*package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Expert;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.repository.SinistreRepository;
import com.vermeg.sinistpro.service.SinistreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sinistres")
public class SinistreController {
    @Autowired
    private SinistreRepository repository;

    @PostMapping
    public Sinistre createSinistre(@RequestBody Sinistre sinistre) {
        return repository.save(sinistre);
    }

    @GetMapping
    public List<Sinistre> getSinistres() {
        return repository.findAll();
    }*/


package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Expert;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.service.ExpertService;
import com.vermeg.sinistpro.service.SinistreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sinistres")
public class SinistreController {

    private final SinistreService sinistreService;
    private final ExpertService expertService;  // Add ExpertService here

    @Autowired
    public SinistreController(SinistreService sinistreService, ExpertService expertService) {
        this.sinistreService = sinistreService;
        this.expertService = expertService;  // Inject ExpertService here
    }

    @PutMapping("/{id}/expert")
    public Sinistre assignExpertToSinistre(@PathVariable Long id, @RequestParam Long expertId) {
        Sinistre sinistre = sinistreService.findSinistreById(id);
        Expert expert = expertService.findExpertById(expertId);  // This should now work
        sinistreService.attribuerExpert(sinistre.getId(), expert.getId());
        return sinistre;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sinistre> getSinistreStatus(@PathVariable Long id) {
        Sinistre sinistre = sinistreService.consulterStatut(id);
        return ResponseEntity.ok(sinistre);
    }
}


/*
 @RestController
@RequestMapping("/sinistres")
public class SinistreController {
    private final SinistreService sinistreService;

    public SinistreController(SinistreService sinistreService) {
        this.sinistreService = sinistreService;
    }

    @PostMapping
    public ResponseEntity<Sinistre> createSinistre(@RequestBody ClaimRequest request) {
        Sinistre createdSinistre = sinistreService.créerSinistre(request); // Call service method
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSinistre); // Return 201 status
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sinistre> getSinistreStatus(@PathVariable Long id) {
        Sinistre sinistre = sinistreService.consulterStatut(id);
        return ResponseEntity.ok(sinistre);
    }
}
*/