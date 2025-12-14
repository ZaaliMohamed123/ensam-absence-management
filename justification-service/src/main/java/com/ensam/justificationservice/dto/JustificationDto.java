package com.ensam.justificationservice.dto;

import com.ensam.justificationservice.entities.Justification.JustificationStatus;
import com.ensam.justificationservice.entities.Justification.JustificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JustificationDto {

    private Long id;

    @NotNull(message = "Absence ID is required")
    private Long absenceId;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Type is required")
    private JustificationType type;

    @NotBlank(message = "Description is required")
    private String description;

    private String documentUrl;
    private String documentName;
    private JustificationStatus status;
    private String rejectionReason;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String submittedBy;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}
