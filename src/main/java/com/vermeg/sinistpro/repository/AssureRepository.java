package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.Assure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssureRepository extends JpaRepository<Assure, Long> {

    // Custom query method to find Assure by numeroPolice
    Assure findByNumeroPolice(String numeroPolice);
}
