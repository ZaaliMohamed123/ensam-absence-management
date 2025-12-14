package com.ensam.academicservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicClassDto {
    private Long id;

    @NotBlank(message = "Class name is required")
    private String className;

    @NotBlank(message = "Level is required")
    private String level;

    @NotBlank(message = "Branch is required")
    private String branch;

    @NotNull(message = "Academic year is required")
    private Integer academicYear;

    private String delegateId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
