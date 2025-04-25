package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String contact;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "admin")
    private List<Sinistre> sinistres;

    public void setId(Long id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSinistres(List<Sinistre> sinistres) {
        this.sinistres = sinistres;
    }

    public void superviserTraitementSinistres(Sinistre sinistre) {
        sinistre.autoUpdateStatus();
    }

    public void genererRapportSinistres() {
        for (Sinistre sinistre : sinistres) {
            System.out.println("Claim ID: " + sinistre.getId() + ", Status: " + sinistre.getStatus());
        }
    }

    public void attribuerExpert(Sinistre sinistre, Expert expert) {
        sinistre.setExpert(expert);
    }

    public void gererDetectionFraude(Sinistre sinistre) {
        // TODO: Implement fraud detection logic
    }

    public BigDecimal calculerCoutTotalSinistres() {
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Sinistre sinistre : sinistres) {
            totalCost = totalCost.add(sinistre.calculerMontantIndemnisation());
        }
        return totalCost;
    }

    public void superviserOptimisationProcessus() {
        // TODO: Implement logic to optimize claim processing
    }
}