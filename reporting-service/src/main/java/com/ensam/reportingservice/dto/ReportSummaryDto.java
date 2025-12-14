package com.ensam.reportingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDto {
    private Long totalReports;
    private Long publishedReports;
    private Long draftReports;
    private Long reportsWithAlerts;
    private Double averageAbsenceRate;
}
