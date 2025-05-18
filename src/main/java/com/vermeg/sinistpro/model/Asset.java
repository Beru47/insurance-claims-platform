package com.vermeg.sinistpro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // e.g., "vehicle", "property", "personal_item"
    private String identifier; // e.g., VIN for vehicles, serial number for items
    private String description;
    private BigDecimal price; // Insured value
    private String condition; // e.g., "new", "used", "refurbished"

    // Vehicle-specific fields
    private String vehicleType; // e.g., "Car", "Motorcycle"
    private String vehicleMake; // e.g., "Toyota"
    private String vehicleModel; // e.g., "Corolla"
    private Integer vehicleYear; // e.g., 2020
    private Long kilometers; // e.g., 50000

    // Property-specific fields
    private String propertyAddress;
    private String propertyType; // e.g., "House", "Apartment"

    // Personal item-specific fields (e.g., watch, phone)
    private String itemCategory; // e.g., "Watch", "Phone", "Jewelry"
    private String itemBrand; // e.g., "Rolex", "Apple"
    private String itemModel; // e.g., "Submariner", "iPhone 14"

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    @JsonBackReference
    private Policy policy;
}
/*package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // e.g., "vehicle", "property"
    private String identifier; // e.g., VIN for vehicles, address for property
    private String description;
    private BigDecimal price; // For insurance pricing
    private Long kilometers; // For vehicles
    private Integer year; // For vehicles
    private String condition; // e.g., "new", "used"

    // Vehicle-specific fields
    private String vehicleType;
    private String vehicleMake;
    private String vehicleModel;

    // Property-specific fields
    private String propertyAddress;
    private String propertyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private Policy policy;
}*/