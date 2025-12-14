package com.ensam.academicservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private AcademicClass academicClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false)
    private String teacherId; // User ID de l'enseignant

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek; // MONDAY, TUESDAY, etc.

    @Column(nullable = false)
    private LocalTime startTime; // Ex: 08:00

    @Column(nullable = false)
    private LocalTime endTime; // Ex: 10:00

    @Column(nullable = false)
    private String room; // Ex: "Amphi A", "Salle 201"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType sessionType; // COURSE, TD, TP

    @Column(nullable = false)
    private Boolean isRecurring = true; // Si c'est récurrent ou ponctuel

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

    public enum SessionType {
        COURSE,  // Cours magistral
        TD,      // Travaux dirigés
        TP       // Travaux pratiques
    }
}
