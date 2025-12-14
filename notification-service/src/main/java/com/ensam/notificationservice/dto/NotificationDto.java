package com.ensam.notificationservice.dto;

import com.ensam.notificationservice.entities.Notification.NotificationStatus;
import com.ensam.notificationservice.entities.Notification.NotificationType;
import com.ensam.notificationservice.entities.Notification.RecipientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long id;

    @NotBlank(message = "Recipient ID is required")
    private String recipientId;

    @NotNull(message = "Recipient type is required")
    private RecipientType recipientType;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Type is required")
    private NotificationType type;

    private NotificationStatus status;
    private Boolean isRead;
    private Long relatedAbsenceId;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
