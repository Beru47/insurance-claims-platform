package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.Guarantee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> {

    Optional<Guarantee> findByName(String name);

    boolean existsByName(String name);

    List<Guarantee> findByPoliciesId(Long policyId);
}