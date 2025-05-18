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
    private String type; // e.g., "Accident",theft
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

    // Accident-specific fields
    private String accidentType; // e.g., "Collision", "Theft"
    private Boolean thirdPartyInvolved;
    private String policeReportNumber;

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

    public ClaimStatus getStatus() {
        return status;
    }
}

/*package com.vermeg.sinistpro.model;

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

    public BigDecimal getMontantIndemnisation() {
        return montantIndemnisation;
    }
}*/
