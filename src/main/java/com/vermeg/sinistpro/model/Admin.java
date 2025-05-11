package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is mandatory")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Contact is mandatory")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Column(nullable = false)
    private String contact;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Sinistre> sinistres = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastLogin;

    @ElementCollection
    @CollectionTable(name = "admin_audit_trail", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "audit_entry")
    private List<String> auditTrail = new ArrayList<>();
}