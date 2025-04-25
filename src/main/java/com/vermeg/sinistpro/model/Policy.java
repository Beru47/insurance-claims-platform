package com.vermeg.sinistpro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String numeroPolice;
    private String typeAssurance; // e.g., "Auto", "Home"
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    @ElementCollection
    private List<String> guarantees;
    private boolean valid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonBackReference
    private Client client;
    @OneToMany(mappedBy = "policy")
    private List<Asset> assets;
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Sinistre> sinistres;
}