package com.vermeg.sinistpro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Sinistre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private LocalDateTime date;
    private String lieu;
    private String description;
    private Double fraudScore = 0.0;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    private Integer priorityScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    @JsonBackReference
    private Policy policy;

    @ManyToOne
    @JoinColumn(name = "expert_id")
    private Expert expert;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    private BigDecimal montantIndemnisation;

    @Column(unique = true)
    private String numeroSinistre;

    // Vehicle-specific fields
    private String vehicleType;
    private String vehicleMake;
    private String vehicleModel;
    private String vehicleYear;
    private String vin;
    private String accidentType;
    private Boolean thirdPartyInvolved;
    private String policeReportNumber;

    // Home-specific fields
    private String propertyAddress;
    private String damageType;
    private String damageExtent;
    @ElementCollection
    private List<String> affectedAreas;
    private Boolean emergencyServicesCalled;

    // Health-specific fields
    private String medicalCondition;
    private String treatmentLocation;
    private LocalDateTime treatmentDate;
    private String doctorName;
    private Double medicalBillAmount;
    private Boolean hospitalizationRequired;

    // Property-specific fields
    private String propertyType;
    private String incidentCause;
    private String propertyDamageDescription;
    private Double estimatedLossValue;
    private Boolean businessInterruption;

    // Media references
    @OneToMany(mappedBy = "sinistre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaReference> mediaReferences = new ArrayList<>();

    public void autoUpdateStatus() {
        if (this.fraudScore > 0.7) {
            this.status = ClaimStatus.SUSPECTED_FRAUD;
        } else if (this.priorityScore >= 80) {
            this.status = ClaimStatus.URGENT;
        } else {
            this.status = ClaimStatus.PENDING;
        }
    }

    public BigDecimal calculerMontantIndemnisation() {
        return BigDecimal.ZERO; // To be implemented by IndemnificationService
    }
}


/*package com.vermeg.sinistpro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sinistre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private LocalDateTime date;
    private String lieu;
    private String description;
    private double fraudScore;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;
    private Integer priorityScore;  // New field for priority

    /*@ManyToOne
     @JoinColumn(name = "assure_id") // Explicit foreign key
     private Assure assure;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    @JsonBackReference
    private Policy policy;

    @ManyToOne
    @JoinColumn(name = "expert_id") // Explicit foreign key
    private Expert expert;

    // Add relationship with Admin
    @ManyToOne
    @JoinColumn(name = "admin_id") // Foreign key column in the sinistre table
    private Admin admin; // <-- This field must exist for the mappedBy in Admin
    private BigDecimal montantIndemnisation;
    @Column(unique = true)
    private String numeroSinistre;

    // Vehicle-specific fields
    private String vehicleType;
    private String vehicleMake;
    private String vehicleModel;
    private String vehicleYear;
    private String vin;
    private String accidentType;
    private Boolean thirdPartyInvolved;
    private String policeReportNumber;

    // Home-specific fields
    private String propertyAddress;
    private String damageType;
    private String damageExtent;
    @ElementCollection
    private List<String> affectedAreas;
    private Boolean emergencyServicesCalled;

    // Health-specific fields
    private String medicalCondition;
    private String treatmentLocation;
    private LocalDateTime treatmentDate;
    private String doctorName;
    private Double medicalBillAmount;
    private Boolean hospitalizationRequired;

    // Property-specific fields
    private String propertyType;
    private String incidentCause;
    private String propertyDamageDescription;
    private Double estimatedLossValue;
    private Boolean businessInterruption;

    // Media references
    @OneToMany(mappedBy = "sinistre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaReference> mediaReferences = new ArrayList<>();




    /*@ManyToOne
    private Assure assure;  // Assure associated with the Sinistre
    @ManyToOne
    private Expert expert;  // Expert associated with the Sinister.

    // Corrected Constructor
    public Sinistre(String type, LocalDateTime date, String lieu, String description, ClaimStatus claimStatus, int priority, Policy policy) {
        this.type = type;
        this.date = date;
        this.lieu = lieu;
        this.description = description;
        this.status = claimStatus;
        this.priorityScore = priority;
        this.policy = policy;
    }

    public void setFraudScore(double fraudScore) {
        this.fraudScore = fraudScore;
    }

    // Getters and Setters (auto-generated by Lombok)

    // Method to update the claim status
    public void mettreÀJourStatut(ClaimStatus newStatus) {
        this.status = newStatus;
    }

    // Method to automatically update the status based on priority
    public void autoUpdateStatus() {
        if (this.priorityScore >= 8) {
            this.status = ClaimStatus.URGENT;
        } else if (this.priorityScore >= 5) {
            this.status = ClaimStatus.IN_PROGRESS;
        } else {
            this.status = ClaimStatus.PENDING;
        }
    }

    // Method to calculate the compensation amount based on the claim type
    public BigDecimal calculerMontantIndemnisation() {
        if (this.type.equalsIgnoreCase("Accident")) {
            return BigDecimal.valueOf(1000);
        } else if (this.type.equalsIgnoreCase("Incendie")) {
            return BigDecimal.valueOf(5000);
        }
        return BigDecimal.ZERO;
    }

    // Setter method for Expert
    public void setExpert(Expert expert) {
        this.expert = expert;
    }

    // Setter method for PriorityScore (automatically calculates priority score if not provided)
    public void setPriorityScore(Integer priorityScore) {
        if (priorityScore == null) {
            // Automatically calculate the priority based on business logic
            this.priorityScore = calculatePriorityScore();
        } else {
            this.priorityScore = priorityScore;
        }
    }

    // Method to calculate priority score (example)
    private Integer calculatePriorityScore() {
        // Replace with actual logic to calculate priority based on claim data
        return 5; // Example static value
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }


}*/