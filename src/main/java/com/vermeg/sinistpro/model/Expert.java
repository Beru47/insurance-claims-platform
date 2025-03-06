package com.vermeg.sinistpro.model;

import jakarta.persistence.*;

import java.util.List;

@Entity

public class Expert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String specialite;
    private String contact;

    // Fix: Add @OneToMany relationship
    @OneToMany(mappedBy = "expert")  // "expert" refers to the field in Sinistre
    private List<Sinistre> sinistresAttributs;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public List<Sinistre> getSinistresAttributs() {
        return sinistresAttributs;
    }

    public void setSinistresAttributs(List<Sinistre> sinistresAttributs) {
        this.sinistresAttributs = sinistresAttributs;
    }
}
