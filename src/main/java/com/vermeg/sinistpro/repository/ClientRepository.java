package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByCin(String cin);

    Optional<Client> findByTelephone(String telephone);
}