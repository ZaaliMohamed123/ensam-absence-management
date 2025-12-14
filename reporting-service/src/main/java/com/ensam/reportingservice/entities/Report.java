package com.ensam.reportingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType; // STUDENT, CLASS, MODULE, TEACHER, GLOBAL

    @Column(nullable = false)
    private String targetId; // ID de l'entité ciblée

    private String targetName; // Nom pour affichage

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportPeriod period; // DAILY, WEEKLY, MONTHLY, SEMESTER, CUSTOM

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false, length = 5000)
    private String reportData; // JSON contenant les données détaillées

    // Statistiques générales
    private Long totalAbsences;
    private Long justifiedAbsences;
    private Long unjustifiedAbsences;
    private Long pendingJustifications;
    private Double absenceRate; // Pourcentage

    // Alertes et seuils
    private Boolean hasAlert; // Si seuil dépassé
    private String alertMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status; // DRAFT, GENERATED, PUBLISHED, ARCHIVED

    @Column(nullable = false)
    private String generatedBy; // User ID

    private String publishedBy; // User ID (si publié)
    private LocalDateTime publishedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ReportType {
        STUDENT,   // Rapport individuel étudiant
        CLASS,     // Rapport de classe
        MODULE,    // Rapport par module
        TEACHER,   // Rapport par enseignant
        GLOBAL     // Rapport global établissement
    }

    public enum ReportPeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
        SEMESTER,
        ANNUAL,
        CUSTOM
    }

    public enum ReportStatus {
        DRAFT,      // Brouillon
        GENERATED,  // Généré
        PUBLISHED,  // Publié
        ARCHIVED    // Archivé
    }
}
