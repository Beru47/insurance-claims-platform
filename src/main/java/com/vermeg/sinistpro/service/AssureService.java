package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Assure;
import com.vermeg.sinistpro.repository.AssureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssureService {

    @Autowired
    private AssureRepository assureRepository;

    public Assure findAssureByNumeroPolice(String numeroPolice) {
        return assureRepository.findByNumeroPolice(numeroPolice);
    }

    public Assure saveAssure(Assure assure) {
        return assureRepository.save(assure);
    }

    public boolean validatePolicy(String numeroPolice) {
        Assure assure = assureRepository.findByNumeroPolice(numeroPolice);
        return assure != null && assure.isPolicyValid();
    }
}
