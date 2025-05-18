package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Sinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    @Query("SELECT s FROM Sinistre s WHERE s.expert.id = :expertId AND s.status IN ('PENDING', 'URGENT')")
    List<Sinistre> findByExpertIdAndStatusNotClosed(@Param("expertId") Long expertId);

    List<Sinistre> findByLieu(String lieu);

    List<Sinistre> findByStatus(ClaimStatus status);

    List<Sinistre> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM Sinistre s WHERE s.priorityScore > :score")
    List<Sinistre> findHighPrioritySinistres(@Param("score") int score);

    Optional<Sinistre> findByNumeroSinistre(String numeroSinistre);

    List<Sinistre> findByPolicyClientId(Long clientId);

    List<Sinistre> findTop5ByOrderByDateDesc();

    @Query("SELECT s FROM Sinistre s WHERE s.expert.id = :expertId")
    List<Sinistre> findByExpertId(@Param("expertId") Long expertId);
}

/*package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Sinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    @Query("SELECT s FROM Sinistre s WHERE s.expert.id = :expertId AND s.status IN ('PENDING', 'URGENT')")
    List<Sinistre> findByExpertIdAndStatusNotClosed(@Param("expertId") Long expertId);

    // Find all sinistres by location
    List<Sinistre> findByLieu(String lieu);

    // Find sinistres by status (PENDING, APPROVED, REJECTED, etc.)
    List<Sinistre> findByStatus(ClaimStatus status);

    // Find sinistres declared within a specific date range
    List<Sinistre> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // Find high-priority sinistres (e.g., score > threshold)
    @Query("SELECT s FROM Sinistre s WHERE s.priorityScore > :score")
    List<Sinistre> findHighPrioritySinistres(@Param("score") int score);

    Optional<Sinistre> findByNumeroSinistre(String numeroSinistre);

    List<Sinistre> findByPolicyClientId(Long clientId);

    // Find the most recent sinistres, ordered by date descending
    List<Sinistre> findTop5ByOrderByDateDesc();

    @Query("SELECT s FROM Sinistre s WHERE s.expert.id = :expertId")
    List<Sinistre> findByExpertId(@Param("expertId") Long expertId);
}*/