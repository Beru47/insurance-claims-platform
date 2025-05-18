package com.vermeg.sinistpro.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class MediaReference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath; // Path or URL to the file (e.g., "/uploads/sinistre_1_photo.jpg" or S3 URL)

    private String fileType; // e.g., "image/jpeg", "video/mp4"
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "sinistre_id")
    private Sinistre sinistre;
}