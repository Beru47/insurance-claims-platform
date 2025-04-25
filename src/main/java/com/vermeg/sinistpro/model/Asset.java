package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type; // e.g., "Car", "House"
    private String identifier; // e.g., VIN for car, address for house
    private String description; // e.g., "Toyota Corolla 2020", "123 Main St"
    @ManyToOne
    @JoinColumn(name = "policy_id")
    private Policy policy;
}