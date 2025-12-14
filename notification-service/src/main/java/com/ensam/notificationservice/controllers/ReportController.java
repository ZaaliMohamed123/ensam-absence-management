package com.ensam.notificationservice.controllers;

import com.ensam.notificationservice.dto.ReportDto;
import com.ensam.notificationservice.entities.Report.ReportPeriod;
import com.ensam.notificationservice.entities.Report.ReportType;
import com.ensam.notificationservice.services.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ReportDto> generateReport(
            @Valid @RequestBody ReportDto dto,
            Authentication authentication) {
        String generatedBy = authentication.getName();
        ReportDto created = reportService.generateReport(dto, generatedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportDto>> getAllReports() {
        List<ReportDto> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ReportDto> getReportById(@PathVariable Long id) {
        ReportDto report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/target/{targetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ReportDto>> getReportsByTargetId(@PathVariable String targetId) {
        List<ReportDto> reports = reportService.getReportsByTargetId(targetId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/type/{reportType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReportDto>> getReportsByType(@PathVariable ReportType reportType) {
        List<ReportDto> reports = reportService.getReportsByType(reportType);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/period/{period}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReportDto>> getReportsByPeriod(@PathVariable ReportPeriod period) {
        List<ReportDto> reports = reportService.getReportsByPeriod(period);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/target/{targetId}/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ReportDto>> getRecentReports(
            @PathVariable String targetId,
            @RequestParam(defaultValue = "30") int daysBack) {
        List<ReportDto> reports = reportService.getRecentReports(targetId, daysBack);
        return ResponseEntity.ok(reports);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
