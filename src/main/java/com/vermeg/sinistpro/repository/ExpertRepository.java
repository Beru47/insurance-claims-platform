package com.vermeg.sinistpro.repository;

import com.vermeg.sinistpro.model.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {
    // Find all experts with the EXPERT role
    @Query("SELECT e FROM Expert e JOIN e.user u JOIN u.roles r WHERE r.name = 'ROLE_EXPERT'")
    List<Expert> findAllWithExpertRole();

    // Find experts by specialization
    List<Expert> findBySpecialite(String specialite);

    // Find experts by specialization and with EXPERT role
    @Query("SELECT e FROM Expert e JOIN e.user u JOIN u.roles r WHERE e.specialite = :specialite AND r.name = 'ROLE_EXPERT'")
    List<Expert> findBySpecialiteAndExpertRole(@Param("specialite") String specialite);

    // Find experts by region
    @Query("SELECT e FROM Expert e JOIN e.user u JOIN u.roles r WHERE " +
            "e.location = :location AND r.name = 'ROLE_EXPERT'")
    List<Expert> findByLocationAndExpertRole(@Param("location") String location);

    // Count experts by specialization
    @Query("SELECT e.specialite, COUNT(e) FROM Expert e GROUP BY e.specialite")
    List<Object[]> countExpertsBySpecialite();

    // Count experts by location
    @Query("SELECT e.location, COUNT(e) FROM Expert e GROUP BY e.location")
    List<Object[]> countExpertsByLocation();

    // Find experts with lowest workload (requires custom implementation in ExpertService)
    // This query only retrieves experts, workload calculation happens in service
    @Query("SELECT e FROM Expert e JOIN e.user u JOIN u.roles r WHERE r.name = 'ROLE_EXPERT' " +
            "ORDER BY e.id")
    List<Expert> findAllExpertsForWorkloadCalculation();
}