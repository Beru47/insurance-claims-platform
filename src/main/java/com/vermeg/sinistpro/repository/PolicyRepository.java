package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByNumeroPolice(String numeroPolice);
}