package com.vermeg.sinistpro.repository;


import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Sinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    // Find all sinistres by location
    List<Sinistre> findByLieu(String lieu);

    // Find sinistres by status (PENDING, APPROVED, REJECTED, etc.)
    List<Sinistre> findByStatus(ClaimStatus status);

    // Find sinistres declared within a specific date range
    List<Sinistre> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // Find high-priority sinistres (e.g., score > threshold)
    @Query("SELECT s FROM Sinistre s WHERE s.priorityScore > :score")
    List<Sinistre> findHighPrioritySinistres(int score);
}


/*@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    // Find all sinistres by location
    List<Sinistre> findByLieu(String lieu);

    // Find sinistres by status (PENDING, APPROVED, REJECTED, etc.)
    List<Sinistre> findByStatus(ClaimStatus status);

    // Find sinistres declared within a specific date range
    List<Sinistre> findByDateDeclarationBetween(LocalDate startDate, LocalDate endDate);

    // Find sinistres for a specific assured person (by ID)
    List<Sinistre> findByAssureId(Long assureId);

    // Find high-priority sinistres (e.g., score > threshold)
    @Query("SELECT s FROM Sinistre s WHERE s.priorityScore > :score")
    List<Sinistre> findHighPrioritySinistres(int score);

    // Find the latest sinistre for an assured person
    Optional<Sinistre> findTopByAssureIdOrderByDateDesc(Long assureId);
}*/
