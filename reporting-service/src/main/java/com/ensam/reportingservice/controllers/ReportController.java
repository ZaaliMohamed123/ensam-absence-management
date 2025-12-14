package com.ensam.reportingservice.controllers;

import com.ensam.reportingservice.dto.ReportDto;
import com.ensam.reportingservice.dto.ReportSummaryDto;
import com.ensam.reportingservice.entities.Report.ReportPeriod;
import com.ensam.reportingservice.entities.Report.ReportStatus;
import com.ensam.reportingservice.entities.Report.ReportType;
import com.ensam.reportingservice.services.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReportDto>> getReportsByStatus(@PathVariable ReportStatus status) {
        List<ReportDto> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReportDto>> getReportsWithAlerts() {
        List<ReportDto> reports = reportService.getReportsWithAlerts();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReportDto>> getRecentReports(
            @RequestParam(defaultValue = "30") int days) {
        List<ReportDto> reports = reportService.getRecentReports(days);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/daterange")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ReportDto>> getReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ReportDto> reports = reportService.getReportsByDateRange(startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ReportDto> getLatestReport(
            @RequestParam String targetId,
            @RequestParam ReportType reportType,
            @RequestParam ReportPeriod period) {
        ReportDto report = reportService.getLatestReport(targetId, reportType, period);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ReportSummaryDto> getReportsSummary() {
        ReportSummaryDto summary = reportService.getReportsSummary();
        return ResponseEntity.ok(summary);
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ReportDto> publishReport(
            @PathVariable Long id,
            Authentication authentication) {
        String publishedBy = authentication.getName();
        ReportDto published = reportService.publishReport(id, publishedBy);
        return ResponseEntity.ok(published);
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportDto> archiveReport(@PathVariable Long id) {
        ReportDto archived = reportService.archiveReport(id);
        return ResponseEntity.ok(archived);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ReportDto> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportDto dto) {
        ReportDto updated = reportService.updateReport(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
