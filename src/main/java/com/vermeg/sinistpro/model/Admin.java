package com.vermeg.sinistpro.model;

import java.util.List;

public class Admin {

    private Long id;
    private String nom;
    private String contact;
    private List<Assure> assures;  // Assures managed by the admin
    private List<Sinistre> sinistres;  // Claims supervised by the admin

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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public List<Assure> getAssures() {
        return assures;
    }

    public void setAssures(List<Assure> assures) {
        this.assures = assures;
    }

    public List<Sinistre> getSinistres() {
        return sinistres;
    }

    public void setSinistres(List<Sinistre> sinistres) {
        this.sinistres = sinistres;
    }

    // Method to manage assures (add/remove/update)
    public void gererAssure(Assure assure, String action) {
        if ("add".equalsIgnoreCase(action)) {
            assures.add(assure);
        } else if ("remove".equalsIgnoreCase(action)) {
            assures.remove(assure);
        } else if ("update".equalsIgnoreCase(action)) {
            // Implement logic to update assure information
        }
    }

    // Method to supervise claims processing
    public void superviserTraitementSinistres(Sinistre sinistre) {
        // Implement logic to supervise claim processing
        // Example: update claim status, review documentation, etc.
    }

    // Method to generate a report on claims status
    public void genererRapportSinistres() {
        for (Sinistre sinistre : sinistres) {
            System.out.println("Claim ID: " + sinistre.getId() + ", Status: " + sinistre.getStatus());
        }
    }

    // Method to manage experts and repairers (assign to a claim)
    public void attribuerExpert(Sinistre sinistre, Expert expert) {
        sinistre.setExpert(expert);
    }

    // Method to manage fraud detection tasks
    public void gererDetectionFraude(Sinistre sinistre) {
        // Fraud detection logic
    }

    // Method to calculate the overall cost of all claims under supervision
    public double calculerCoutTotalSinistres() {
        double totalCost = 0;
        for (Sinistre sinistre : sinistres) {
            totalCost += sinistre.calculerMontantIndemnisation().doubleValue();
        }
        return totalCost;
    }

    // Method to oversee optimization of the claim process
    public void superviserOptimisationProcessus() {
        // Logic for supervising the optimization of the claim management process
    }
}
