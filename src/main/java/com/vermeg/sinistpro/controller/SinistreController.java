package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.model.SinistreDTO;
import com.vermeg.sinistpro.model.SinistreRequest;
import com.vermeg.sinistpro.service.DashboardService;
import com.vermeg.sinistpro.service.SinistreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sinistres")
public class SinistreController {

    private final SinistreService sinistreService;
    private final DashboardService dashboardService;

    public SinistreController(SinistreService sinistreService, DashboardService dashboardService) {
        this.sinistreService = sinistreService;
        this.dashboardService = dashboardService;
    }

    @PostMapping("/declare")
    public ResponseEntity<Sinistre> declareSinistre(
            @RequestPart(value = "request") SinistreRequest request,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles) {

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            request.setMediaFiles(mediaFiles);
        } else {
            request.setMediaFiles(new ArrayList<>());
        }

        Sinistre createdSinistre = sinistreService.créerSinistre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSinistre);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SinistreDTO> getSinistre(@PathVariable Long id) {
        Sinistre sinistre = sinistreService.consulterStatut(id);
        SinistreDTO sinistreDTO = dashboardService.convertToDTO(sinistre);
        return ResponseEntity.ok(sinistreDTO);
    }

    @GetMapping
    public ResponseEntity<List<SinistreDTO>> getAllSinistres() {
        List<Sinistre> sinistres = sinistreService.getAllSinistres();
        List<SinistreDTO> sinistreDTOs = sinistres.stream()
                .map(dashboardService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sinistreDTOs);
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


/*package com.vermeg.sinistpro.controller;


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

    @PatchMapping("/{id}/status")   //this is used to change the status of the sinsitre by the admin
    public ResponseEntity<Sinistre> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {

        ClaimStatus newStatus = ClaimStatus.valueOf(statusUpdate.get("status"));
        Sinistre updatedSinistre = sinistreService.updateStatus(id, newStatus);
        return ResponseEntity.ok(updatedSinistre);
    }
}*/
