package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sinistre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private LocalDate date;
    private String lieu;
    private String description;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;
    private Integer priorityScore;  // New field for priority
}

