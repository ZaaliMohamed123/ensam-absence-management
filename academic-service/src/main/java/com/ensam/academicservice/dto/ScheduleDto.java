package com.ensam.academicservice.dto;

import com.ensam.academicservice.entities.Schedule.SessionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    private Long id;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Module ID is required")
    private Long moduleId;

    @NotBlank(message = "Teacher ID is required")
    private String teacherId;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Room is required")
    private String room;

    @NotNull(message = "Session type is required")
    private SessionType sessionType;

    @NotNull(message = "Is recurring is required")
    private Boolean isRecurring;

    private String className;
    private String moduleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
