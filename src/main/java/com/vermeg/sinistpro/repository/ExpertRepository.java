package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {
    // You can add custom queries if necessary
}
