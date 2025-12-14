package com.ensam.academicservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "academic_classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String className; // Ex: "GI-2A-1", "GI-3A-2"

    @Column(nullable = false)
    private String level; // Ex: "1A", "2A", "3A"

    @Column(nullable = false)
    private String branch; // Ex: "Génie Informatique", "Génie Civil"

    @Column(nullable = false)
    private Integer academicYear; // Ex: 2024, 2025

    private String delegateId; // User ID du délégué de classe

    @OneToMany(mappedBy = "academicClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Module> modules = new ArrayList<>();

    @OneToMany(mappedBy = "academicClass", cascade = CascadeType.ALL, orphanRemoval = true)
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
