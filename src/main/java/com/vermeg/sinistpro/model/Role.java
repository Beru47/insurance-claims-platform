package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles") // Explicitly map to 'roles' table
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}