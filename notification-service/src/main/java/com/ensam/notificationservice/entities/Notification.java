package com.ensam.notificationservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientId; // User ID du destinataire

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipientType recipientType; // STUDENT, TEACHER, ADMIN

    @Column(nullable = false)
    private String subject; // Objet de la notification

    @Column(nullable = false, length = 2000)
    private String message; // Contenu du message

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // ABSENCE_ALERT, WEEKLY_REPORT, MONTHLY_REPORT, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status; // PENDING, SENT, FAILED

    @Column(nullable = false)
    private Boolean isRead = false;

    private Long relatedAbsenceId; // ID de l'absence liée (optionnel)

    private LocalDateTime sentAt;

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

    public enum RecipientType {
        STUDENT,
        TEACHER,
        ADMIN
    }

    public enum NotificationType {
        ABSENCE_ALERT,      // Alerte d'absence
        WEEKLY_REPORT,      // Rapport hebdomadaire
        MONTHLY_REPORT,     // Rapport mensuel
        ABSENCE_THRESHOLD,  // Seuil d'absences atteint
        SYSTEM_MESSAGE      // Message système
    }

    public enum NotificationStatus {
        PENDING,   // En attente d'envoi
        SENT,      // Envoyée
        FAILED     // Échec d'envoi
    }
}
