/*package com.vermeg.sinistpro.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

// HEDHA ZAYEDD 3awedhta b Replace Assure with Client and Policy. If you need to keep Assure for legacy reasons, update it to reference Client:
@Entity
public class Assure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Unique ID for the Assure
    private String nom;  // Full name of the Assure
    private String prenom;  // First name of the Assure
    private Date dateNaissance;  // Date of birth
    private String email;  // Email address
    private String telephone;  // Phone number
    private String adresse;  // Address of the Assure
    private String nationalite;  // Nationality of the Assure
    @ElementCollection
    @CollectionTable(name = "assure_guarantees", joinColumns = @JoinColumn(name = "assure_id"))
    @Column(name = "guarantee") // Singular name for each entry
    private List<String> guarantees;
    private String sexe;  // Gender of the Assure

    // Insurance Policy Details
    @Column(name = "numero_police", unique = true, nullable = false)
    private String numeroPolice;  // Insurance policy number
    private Date dateDebutPolice;  // Start date of the policy
    private Date dateFinPolice;  // End date of the policy
    private String typeAssurance;  // Type of insurance (e.g., Health, Car, Property)


    // One-to-many relationship with Sinistre (Claim)
    @OneToMany(mappedBy = "assure")  // 'assure' is the field in Sinistre
    private List<Sinistre> sinistres;  // List of claims (sinistres) made by the Assure

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getNumeroPolice() {
        return numeroPolice;
    }

    public void setNumeroPolice(String numeroPolice) {
        this.numeroPolice = numeroPolice;
    }

    public Date getDateDebutPolice() {
        return dateDebutPolice;
    }

    public void setDateDebutPolice(Date dateDebutPolice) {
        this.dateDebutPolice = dateDebutPolice;
    }

    public Date getDateFinPolice() {
        return dateFinPolice;
    }

    public void setDateFinPolice(Date dateFinPolice) {
        this.dateFinPolice = dateFinPolice;
    }

    public String getTypeAssurance() {
        return typeAssurance;
    }

    public void setTypeAssurance(String typeAssurance) {
        this.typeAssurance = typeAssurance;
    }

    public List<Sinistre> getSinistres() {
        return sinistres;
    }

    public void setSinistres(List<Sinistre> sinistres) {
        this.sinistres = sinistres;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public List<String> getGuarantees() {
        return guarantees;
    }

    public void setGuarantees(List<String> guarantees) {
        this.guarantees = guarantees;
    }

    // Method to declare a new claim (sinistre)
    public void declarerSinistre(Sinistre sinistre) {
        this.sinistres.add(sinistre);
    }

    // Methods

    // Method to calculate the total indemnity for a claim
    /*public double calculerIndemnisation(Sinistre sinistre) {
        // Placeholder for indemnity calculation logic based on policy and claim type
        double indemnity = 0;
        // Indemnity calculation logic here, based on sinistre and policy type partie hedhi na9sa !!!
        return indemnity;
    }

     */
/*

    // Method to check if the policy is valid (e.g., not expired)
    public boolean isPolicyValid() {
        Date currentDate = new Date();
        return currentDate.after(dateDebutPolice) && currentDate.before(dateFinPolice);
    }

    // Method to check if the Assure is eligible for certain services (e.g., fraud detection)
    public boolean isEligibleForService(String service) {
        // Eligibility check logic (e.g., check if policy covers a particular service)
        return true;
    }
}
*/