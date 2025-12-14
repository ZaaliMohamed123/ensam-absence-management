package com.ensam.academicservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {
    private Long id;

    @NotBlank(message = "Module code is required")
    private String moduleCode;

    @NotBlank(message = "Module name is required")
    private String moduleName;

    private String description;

    @NotNull(message = "Credits is required")
    @Min(value = 1, message = "Credits must be at least 1")
    private Integer credits;

    @NotNull(message = "Total hours is required")
    @Min(value = 1, message = "Total hours must be at least 1")
    private Integer totalHours;

    @NotNull(message = "Class ID is required")
    private Long classId;

    private String className;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
