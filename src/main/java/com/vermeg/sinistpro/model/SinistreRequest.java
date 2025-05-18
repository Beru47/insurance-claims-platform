package com.vermeg.sinistpro.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SinistreRequest {
    private String type;
    private LocalDateTime date;
    private String lieu; // For Google Maps address or coordinates
    private String description;
    private Long policyId;
    private List<MultipartFile> mediaFiles;
    private String accidentType;
    private Boolean thirdPartyInvolved;
    private String policeReportNumber;
    
}


/*package com.vermeg.sinistpro.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SinistreRequest {
    private String type;
    private LocalDateTime date;
    private String lieu;
    private String description;
    private Long policyId;
    private List<MultipartFile> mediaFiles;

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

    // Getters and Setters (already handled by @Data)
}*/


/*package com.vermeg.sinistpro.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SinistreRequest {
    private String type;
    private LocalDateTime date;
    private String lieu;
    private String description;
    private Long policyId;
    private List<MultipartFile> mediaFiles; // Added field for photos/videos

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssureId() {
        return policyId;
    }

    public void setpolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public List<MultipartFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(List<MultipartFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }
}



/*package com.vermeg.sinistpro.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SinistreRequest {
    private String type;
    private LocalDateTime date;
    private String lieu;
    private String description;
    private Long policyId;
   /* private String type;
    private LocalDateTime date;
    private String lieu;
    private String description;
    private Long policyId;



import java.time.LocalDateTime;// Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssureId() {
        return policyId;
    }

    public void setpolicyId(Long policyId) {
        this.policyId = policyId;
    }
}*/
