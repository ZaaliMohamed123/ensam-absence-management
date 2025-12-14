package com.ensam.notificationservice.dto;

import com.ensam.notificationservice.entities.Report.ReportPeriod;
import com.ensam.notificationservice.entities.Report.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {

    private Long id;

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    @NotBlank(message = "Target ID is required")
    private String targetId;

    private String targetName;

    @NotNull(message = "Period is required")
    private ReportPeriod period;

    @NotNull(message = "Period start is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end is required")
    private LocalDate periodEnd;

    private String reportData;
    private Long totalAbsences;
    private Long justifiedAbsences;
    private Long unjustifiedAbsences;
    private Double absenceRate;
    private String generatedBy;
    private LocalDateTime createdAt;
}
