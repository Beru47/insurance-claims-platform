package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.repository.SinistreRepository;
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
    }
}