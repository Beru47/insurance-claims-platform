package com.vermeg.sinistpro.model;

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

    */

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
}
