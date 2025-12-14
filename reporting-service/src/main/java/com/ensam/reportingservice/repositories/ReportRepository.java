package com.ensam.reportingservice.repositories;

import com.ensam.reportingservice.entities.Report;
import com.ensam.reportingservice.entities.Report.ReportPeriod;
import com.ensam.reportingservice.entities.Report.ReportStatus;
import com.ensam.reportingservice.entities.Report.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByTargetId(String targetId);

    List<Report> findByTargetIdOrderByCreatedAtDesc(String targetId);

    List<Report> findByReportType(ReportType reportType);

    List<Report> findByPeriod(ReportPeriod period);

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByGeneratedBy(String userId);

    List<Report> findByHasAlert(Boolean hasAlert);

    @Query("SELECT r FROM Report r WHERE r.targetId = :targetId AND r.reportType = :reportType ORDER BY r.createdAt DESC")
    List<Report> findByTargetIdAndType(
            @Param("targetId") String targetId,
            @Param("reportType") ReportType reportType
    );

    @Query("SELECT r FROM Report r WHERE r.reportType = :reportType AND r.period = :period ORDER BY r.createdAt DESC")
    List<Report> findByReportTypeAndPeriod(
            @Param("reportType") ReportType reportType,
            @Param("period") ReportPeriod period
    );

    @Query("SELECT r FROM Report r WHERE r.periodStart >= :startDate AND r.periodEnd <= :endDate ORDER BY r.createdAt DESC")
    List<Report> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Report r WHERE r.targetId = :targetId AND r.status = :status ORDER BY r.createdAt DESC")
    List<Report> findByTargetIdAndStatus(
            @Param("targetId") String targetId,
            @Param("status") ReportStatus status
    );

    @Query("SELECT r FROM Report r WHERE r.createdAt >= :date ORDER BY r.createdAt DESC")
    List<Report> findRecentReports(@Param("date") LocalDateTime date);

    Optional<Report> findFirstByTargetIdAndReportTypeAndPeriodOrderByCreatedAtDesc(
            String targetId,
            ReportType reportType,
            ReportPeriod period
    );
}
