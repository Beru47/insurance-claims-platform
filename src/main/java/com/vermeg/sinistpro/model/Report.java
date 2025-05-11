package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;

    @ManyToOne
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; //PDF FILES

    private LocalDateTime createdAt;
}