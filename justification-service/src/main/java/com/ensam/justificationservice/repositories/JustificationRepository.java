package com.ensam.justificationservice.repositories;

import com.ensam.justificationservice.entities.Justification;
import com.ensam.justificationservice.entities.Justification.JustificationStatus;
import com.ensam.justificationservice.entities.Justification.JustificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JustificationRepository extends JpaRepository<Justification, Long> {

    Optional<Justification> findByAbsenceId(Long absenceId);

    List<Justification> findByStudentId(String studentId);

    List<Justification> findByStudentIdOrderBySubmittedAtDesc(String studentId);

    List<Justification> findByStatus(JustificationStatus status);

    List<Justification> findByType(JustificationType type);

    List<Justification> findBySubmittedBy(String userId);

    List<Justification> findByReviewedBy(String userId);

    Long countByStudentIdAndStatus(String studentId, JustificationStatus status);

    boolean existsByAbsenceId(Long absenceId);
}
