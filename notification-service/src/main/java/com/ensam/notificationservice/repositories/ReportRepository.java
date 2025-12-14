package com.ensam.notificationservice.repositories;

import com.ensam.notificationservice.entities.Report;
import com.ensam.notificationservice.entities.Report.ReportPeriod;
import com.ensam.notificationservice.entities.Report.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByTargetId(String targetId);

    List<Report> findByReportType(ReportType reportType);

    List<Report> findByPeriod(ReportPeriod period);

    List<Report> findByGeneratedBy(String userId);

    @Query("SELECT r FROM Report r WHERE r.targetId = :targetId AND r.periodStart >= :startDate ORDER BY r.createdAt DESC")
    List<Report> findRecentReportsByTargetId(
            @Param("targetId") String targetId,
            @Param("startDate") LocalDate startDate
    );

    @Query("SELECT r FROM Report r WHERE r.reportType = :reportType AND r.period = :period ORDER BY r.createdAt DESC")
    List<Report> findByReportTypeAndPeriod(
            @Param("reportType") ReportType reportType,
            @Param("period") ReportPeriod period
    );
}
