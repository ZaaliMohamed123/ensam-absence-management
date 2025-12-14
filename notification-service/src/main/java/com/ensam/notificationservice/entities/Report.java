package com.ensam.notificationservice.entities;

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
    private ReportType reportType; // STUDENT, CLASS, MODULE, TEACHER

    @Column(nullable = false)
    private String targetId; // ID de l'étudiant, classe, module ou enseignant

    private String targetName; // Nom pour affichage

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportPeriod period; // WEEKLY, MONTHLY, SEMESTER

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false, length = 5000)
    private String reportData; // JSON contenant les données du rapport

    private Long totalAbsences;
    private Long justifiedAbsences;
    private Long unjustifiedAbsences;
    private Double absenceRate;

    @Column(nullable = false)
    private String generatedBy; // User ID qui a généré le rapport

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ReportType {
        STUDENT,   // Rapport d'un étudiant
        CLASS,     // Rapport d'une classe
        MODULE,    // Rapport d'un module
        TEACHER    // Rapport d'un enseignant
    }

    public enum ReportPeriod {
        WEEKLY,
        MONTHLY,
        SEMESTER,
        CUSTOM
    }
}
