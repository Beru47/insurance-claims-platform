/*package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Assure;
import com.vermeg.sinistpro.service.AssureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assures")
public class AssureController {
    private final AssureService assureService;

    public AssureController(AssureService assureService) {
        this.assureService = assureService;
    }

    @PostMapping
    public ResponseEntity<Assure> createAssure(@RequestBody Assure assure) {
        return ResponseEntity.ok(assureService.createAssure(assure));
    }
}
*/
