package com.ensam.absenceservice.dto;

import com.ensam.absenceservice.entities.Absence.AbsenceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceDto {

    private Long id;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Teacher ID is required")
    private String teacherId;

    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    @NotNull(message = "Module ID is required")
    private Long moduleId;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Absence date is required")
    private LocalDate absenceDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Status is required")
    private AbsenceStatus status;

    private String notes;

    private Boolean notified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
