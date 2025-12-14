package com.ensam.absenceservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "absences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentId; // User ID de l'étudiant

    @Column(nullable = false)
    private String teacherId; // User ID de l'enseignant

    @Column(nullable = false)
    private Long scheduleId; // ID du schedule (emploi du temps)

    @Column(nullable = false)
    private Long moduleId; // ID du module

    @Column(nullable = false)
    private Long classId; // ID de la classe

    @Column(nullable = false)
    private LocalDate absenceDate; // Date de l'absence

    @Column(nullable = false)
    private LocalTime startTime; // Heure de début du cours

    @Column(nullable = false)
    private LocalTime endTime; // Heure de fin du cours

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbsenceStatus status; // PENDING, JUSTIFIED, UNJUSTIFIED

    @Column(length = 1000)
    private String notes; // Notes de l'enseignant

    @Column(nullable = false)
    private Boolean notified = false; // Si l'étudiant a été notifié

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

    public enum AbsenceStatus {
        PENDING,      // En attente de justification
        JUSTIFIED,    // Justifiée
        UNJUSTIFIED   // Non justifiée
    }
}
