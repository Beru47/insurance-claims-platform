package com.vermeg.sinistpro.model;

import java.util.List;

public class DashboardDTO {
    private long totalSinistres;
    private long pendingSinistres;
    private List<SinistreDTO> recentSinistres;
    private String lieu;
    private String status;
    private String accidentType;
    private String numeroSinistre;

    // Getters and Setters
    public long getTotalSinistres() {
        return totalSinistres;
    }

    public void setTotalSinistres(long totalSinistres) {
        this.totalSinistres = totalSinistres;
    }

    public long getPendingSinistres() {
        return pendingSinistres;
    }

    public void setPendingSinistres(long pendingSinistres) {
        this.pendingSinistres = pendingSinistres;
    }

    public List<SinistreDTO> getRecentSinistres() {
        return recentSinistres;
    }

    public void setRecentSinistres(List<SinistreDTO> recentSinistres) {
        this.recentSinistres = recentSinistres;
    }
}