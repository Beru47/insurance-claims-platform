package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "experts")
public class Expert {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expert_id_seq")
    @SequenceGenerator(name = "expert_id_seq", sequenceName = "expert_id_seq", allocationSize = 1)
    private Long id;

    private String nom;
    private String specialite; // e.g., vehicle, home, health, property
    private String contact;
    private String location; // e.g., Tunis, Sfax

    // Transient field to calculate workload (not persisted)
    @Transient
    private int workload;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Method to set workload based on assigned claims
    public void setWorkload(int activeClaims, int totalPriorityScore) {
        this.workload = activeClaims * 10 + totalPriorityScore / 10;
    }

    public void setUser(User user) {
        this.user = user;
    }
}


