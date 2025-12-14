package com.ensam.notificationservice.services;

import com.ensam.notificationservice.dto.ReportDto;
import com.ensam.notificationservice.entities.Report;
import com.ensam.notificationservice.entities.Report.ReportPeriod;
import com.ensam.notificationservice.entities.Report.ReportType;
import com.ensam.notificationservice.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public ReportDto generateReport(ReportDto dto, String generatedBy) {
        Report report = mapToEntity(dto);
        report.setGeneratedBy(generatedBy);

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
        return reportRepository.findByTargetId(targetId).stream()
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
    public List<ReportDto> getRecentReports(String targetId, int daysBack) {
        LocalDate startDate = LocalDate.now().minusDays(daysBack);
        return reportRepository.findRecentReportsByTargetId(targetId, startDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new IllegalArgumentException("Report not found with id: " + id);
        }
        reportRepository.deleteById(id);
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
        dto.setAbsenceRate(entity.getAbsenceRate());
        dto.setGeneratedBy(entity.getGeneratedBy());
        dto.setCreatedAt(entity.getCreatedAt());
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
        entity.setAbsenceRate(dto.getAbsenceRate());
        return entity;
    }
}
