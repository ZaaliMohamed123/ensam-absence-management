package com.ensam.absenceservice.repositories;

import com.ensam.absenceservice.entities.Absence;
import com.ensam.absenceservice.entities.Absence.AbsenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    // Find absences by student
    List<Absence> findByStudentId(String studentId);

    // Find absences by teacher
    List<Absence> findByTeacherId(String teacherId);

    // Find absences by class
    List<Absence> findByClassId(Long classId);

    // Find absences by module
    List<Absence> findByModuleId(Long moduleId);

    // Find absences by status
    List<Absence> findByStatus(AbsenceStatus status);

    // Find absences by student and status
    List<Absence> findByStudentIdAndStatus(String studentId, AbsenceStatus status);

    // Find absences by student and date range
    @Query("SELECT a FROM Absence a WHERE a.studentId = :studentId AND a.absenceDate BETWEEN :startDate AND :endDate")
    List<Absence> findByStudentIdAndDateRange(
            @Param("studentId") String studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find absences by class and date range
    @Query("SELECT a FROM Absence a WHERE a.classId = :classId AND a.absenceDate BETWEEN :startDate AND :endDate")
    List<Absence> findByClassIdAndDateRange(
            @Param("classId") Long classId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Count absences by student
    Long countByStudentId(String studentId);

    // Count absences by student and status
    Long countByStudentIdAndStatus(String studentId, AbsenceStatus status);

    // Check if absence exists for specific schedule and student
    Optional<Absence> findByStudentIdAndScheduleIdAndAbsenceDate(
            String studentId,
            Long scheduleId,
            LocalDate absenceDate
    );

    // Find absences by student and module
    List<Absence> findByStudentIdAndModuleId(String studentId, Long moduleId);

    // Count unjustified absences by student and module
    @Query("SELECT COUNT(a) FROM Absence a WHERE a.studentId = :studentId AND a.moduleId = :moduleId AND a.status = 'UNJUSTIFIED'")
    Long countUnjustifiedByStudentIdAndModuleId(
            @Param("studentId") String studentId,
            @Param("moduleId") Long moduleId
    );
}
