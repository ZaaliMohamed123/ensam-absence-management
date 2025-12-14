package com.ensam.justificationservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "justifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Justification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long absenceId; // ID de l'absence liée

    @Column(nullable = false)
    private String studentId; // ID de l'étudiant

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JustificationType type; // MEDICAL, FAMILY, ADMINISTRATIVE, OTHER

    @Column(nullable = false, length = 1000)
    private String description; // Description de la justification

    @Column(length = 500)
    private String documentUrl; // URL du document (certificat médical, etc.)

    @Column(length = 255)
    private String documentName; // Nom du fichier

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JustificationStatus status; // PENDING, APPROVED, REJECTED

    @Column(length = 500)
    private String rejectionReason; // Raison du rejet (si rejeté)

    private String reviewedBy; // User ID de qui a validé/rejeté

    private LocalDateTime reviewedAt; // Date de validation/rejet

    @Column(nullable = false)
    private String submittedBy; // User ID de qui a soumis

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum JustificationType {
        MEDICAL,        // Certificat médical
        FAMILY,         // Raison familiale
        ADMINISTRATIVE, // Raison administrative
        OTHER           // Autre
    }

    public enum JustificationStatus {
        PENDING,   // En attente de validation
        APPROVED,  // Approuvée
        REJECTED   // Rejetée
    }
}
