package com.vermeg.sinistpro.controller;


import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.model.SinistreRequest;
import com.vermeg.sinistpro.service.SinistreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sinistres")
public class SinistreController {

    private final SinistreService sinistreService;

    public SinistreController(SinistreService sinistreService) {
        this.sinistreService = sinistreService;
    }

    @PostMapping("/declare")
    public ResponseEntity<Sinistre> declareSinistre(
            @RequestPart(value = "request") SinistreRequest request,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles) {

        // Set media files if provided
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            request.setMediaFiles(mediaFiles);
        } else {
            request.setMediaFiles(new ArrayList<>());
        }

        Sinistre createdSinistre = sinistreService.créerSinistre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSinistre);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sinistre> getSinistre(@PathVariable Long id) {
        Sinistre sinistre = sinistreService.consulterStatut(id);
        return ResponseEntity.ok(sinistre);
    }

    @GetMapping
    public ResponseEntity<List<Sinistre>> getAllSinistres() {
        List<Sinistre> sinistres = sinistreService.getAllSinistres();
        return ResponseEntity.ok(sinistres);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Sinistre> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {

        ClaimStatus newStatus = ClaimStatus.valueOf(statusUpdate.get("status"));
        Sinistre updatedSinistre = sinistreService.updateStatus(id, newStatus);
        return ResponseEntity.ok(updatedSinistre);
    }
}
/*@RestController
@RequestMapping("/api/sinistres")
public class SinistreController {
    private final SinistreService sinistreService;
    private final ObjectMapper objectMapper;

    public SinistreController(SinistreService sinistreService, ObjectMapper objectMapper) {
        this.sinistreService = sinistreService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Sinistre> déclarerSinistre(
            @RequestPart("claimRequest") String claimRequestJson,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles) throws Exception {
        SinistreRequest request = objectMapper.readValue(claimRequestJson, SinistreRequest.class);
        request.setMediaFiles(mediaFiles);
        Sinistre sinistre = sinistreService.créerSinistre(request);
        return ResponseEntity.ok(sinistre);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sinistre> consulterStatut(@PathVariable Long id) {
        return ResponseEntity.ok(sinistreService.consulterStatut(id));
    }

    @GetMapping
    public ResponseEntity<List<Sinistre>> getSinistres() {
        return ResponseEntity.ok(sinistreService.getAllSinistres());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Sinistre> updateStatus(@PathVariable Long id, @RequestBody ClaimStatus newStatus) {
        return ResponseEntity.ok(sinistreService.updateStatus(id, newStatus));
    }
}



/*import com.vermeg.sinistpro.model.ClaimStatus;
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
