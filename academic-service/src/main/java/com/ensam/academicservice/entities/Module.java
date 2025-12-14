package com.ensam.academicservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String moduleCode; // Ex: "INF201", "MATH301"

    @Column(nullable = false)
    private String moduleName; // Ex: "Base de données", "Analyse numérique"

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer credits; // Nombre de crédits

    @Column(nullable = false)
    private Integer totalHours; // Nombre total d'heures

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private AcademicClass academicClass;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();

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
}
