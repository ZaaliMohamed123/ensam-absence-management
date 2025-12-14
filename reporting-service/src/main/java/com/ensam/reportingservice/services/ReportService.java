package com.ensam.reportingservice.services;

import com.ensam.reportingservice.dto.ReportDto;
import com.ensam.reportingservice.dto.ReportSummaryDto;
import com.ensam.reportingservice.entities.Report;
import com.ensam.reportingservice.entities.Report.ReportPeriod;
import com.ensam.reportingservice.entities.Report.ReportStatus;
import com.ensam.reportingservice.entities.Report.ReportType;
import com.ensam.reportingservice.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public ReportDto generateReport(ReportDto dto, String generatedBy) {
        Report report = mapToEntity(dto);
        report.setStatus(ReportStatus.DRAFT);
        report.setGeneratedBy(generatedBy);
        report.setHasAlert(checkForAlerts(dto));

        if (report.getHasAlert()) {
            report.setAlertMessage(generateAlertMessage(dto));
        }

        Report saved = reportRepository.save(report);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getAllReports() {
        return reportRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportDto getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));
        return mapToDto(report);
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getReportsByTargetId(String targetId) {
        return reportRepository.findByTargetIdOrderByCreatedAtDesc(targetId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getReportsByType(ReportType reportType) {
        return reportRepository.findByReportType(reportType).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getReportsByPeriod(ReportPeriod period) {
        return reportRepository.findByPeriod(period).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getReportsByStatus(ReportStatus status) {
        return reportRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getReportsWithAlerts() {
        return reportRepository.findByHasAlert(true).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getRecentReports(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return reportRepository.findRecentReports(startDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getReportsByDateRange(LocalDate startDate, LocalDate endDate) {
        return reportRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportDto getLatestReport(String targetId, ReportType reportType, ReportPeriod period) {
        Report report = reportRepository.findFirstByTargetIdAndReportTypeAndPeriodOrderByCreatedAtDesc(
                        targetId, reportType, period)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No report found for target: " + targetId + ", type: " + reportType + ", period: " + period));
        return mapToDto(report);
    }

    @Transactional(readOnly = true)
    public ReportSummaryDto getReportsSummary() {
        List<Report> allReports = reportRepository.findAll();

        long total = allReports.size();
        long published = allReports.stream().filter(r -> r.getStatus() == ReportStatus.PUBLISHED).count();
        long draft = allReports.stream().filter(r -> r.getStatus() == ReportStatus.DRAFT).count();
        long withAlerts = allReports.stream().filter(r -> Boolean.TRUE.equals(r.getHasAlert())).count();

        double avgRate = allReports.stream()
                .filter(r -> r.getAbsenceRate() != null)
                .mapToDouble(Report::getAbsenceRate)
                .average()
                .orElse(0.0);

        return new ReportSummaryDto(total, published, draft, withAlerts, avgRate);
    }

    @Transactional
    public ReportDto publishReport(Long id, String publishedBy) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));

        report.setStatus(ReportStatus.PUBLISHED);
        report.setPublishedBy(publishedBy);
        report.setPublishedAt(LocalDateTime.now());

        Report updated = reportRepository.save(report);
        return mapToDto(updated);
    }

    @Transactional
    public ReportDto archiveReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));

        report.setStatus(ReportStatus.ARCHIVED);

        Report updated = reportRepository.save(report);
        return mapToDto(updated);
    }

    @Transactional
    public ReportDto updateReport(Long id, ReportDto dto) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));

        // Only allow updates for draft reports
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new IllegalStateException("Only draft reports can be updated");
        }

        report.setReportData(dto.getReportData());
        report.setTotalAbsences(dto.getTotalAbsences());
        report.setJustifiedAbsences(dto.getJustifiedAbsences());
        report.setUnjustifiedAbsences(dto.getUnjustifiedAbsences());
        report.setPendingJustifications(dto.getPendingJustifications());
        report.setAbsenceRate(dto.getAbsenceRate());
        report.setHasAlert(checkForAlerts(dto));

        if (report.getHasAlert()) {
            report.setAlertMessage(generateAlertMessage(dto));
        }

        Report updated = reportRepository.save(report);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new IllegalArgumentException("Report not found with id: " + id);
        }
        reportRepository.deleteById(id);
    }

    // Helper methods
    private boolean checkForAlerts(ReportDto dto) {
        // Alert if absence rate > 20% or unjustified > 3
        return (dto.getAbsenceRate() != null && dto.getAbsenceRate() > 20.0) ||
                (dto.getUnjustifiedAbsences() != null && dto.getUnjustifiedAbsences() > 3);
    }

    private String generateAlertMessage(ReportDto dto) {
        StringBuilder message = new StringBuilder("Alertes: ");

        if (dto.getAbsenceRate() != null && dto.getAbsenceRate() > 20.0) {
            message.append("Taux d'absence élevé (").append(dto.getAbsenceRate()).append("%). ");
        }

        if (dto.getUnjustifiedAbsences() != null && dto.getUnjustifiedAbsences() > 3) {
            message.append("Absences injustifiées (").append(dto.getUnjustifiedAbsences()).append("). ");
        }

        return message.toString();
    }

    // Mapping methods
    private ReportDto mapToDto(Report entity) {
        ReportDto dto = new ReportDto();
        dto.setId(entity.getId());
        dto.setReportType(entity.getReportType());
        dto.setTargetId(entity.getTargetId());
        dto.setTargetName(entity.getTargetName());
        dto.setPeriod(entity.getPeriod());
        dto.setPeriodStart(entity.getPeriodStart());
        dto.setPeriodEnd(entity.getPeriodEnd());
        dto.setReportData(entity.getReportData());
        dto.setTotalAbsences(entity.getTotalAbsences());
        dto.setJustifiedAbsences(entity.getJustifiedAbsences());
        dto.setUnjustifiedAbsences(entity.getUnjustifiedAbsences());
        dto.setPendingJustifications(entity.getPendingJustifications());
        dto.setAbsenceRate(entity.getAbsenceRate());
        dto.setHasAlert(entity.getHasAlert());
        dto.setAlertMessage(entity.getAlertMessage());
        dto.setStatus(entity.getStatus());
        dto.setGeneratedBy(entity.getGeneratedBy());
        dto.setPublishedBy(entity.getPublishedBy());
        dto.setPublishedAt(entity.getPublishedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private Report mapToEntity(ReportDto dto) {
        Report entity = new Report();
        entity.setReportType(dto.getReportType());
        entity.setTargetId(dto.getTargetId());
        entity.setTargetName(dto.getTargetName());
        entity.setPeriod(dto.getPeriod());
        entity.setPeriodStart(dto.getPeriodStart());
        entity.setPeriodEnd(dto.getPeriodEnd());
        entity.setReportData(dto.getReportData());
        entity.setTotalAbsences(dto.getTotalAbsences());
        entity.setJustifiedAbsences(dto.getJustifiedAbsences());
        entity.setUnjustifiedAbsences(dto.getUnjustifiedAbsences());
        entity.setPendingJustifications(dto.getPendingJustifications());
        entity.setAbsenceRate(dto.getAbsenceRate());
        return entity;
    }
}
