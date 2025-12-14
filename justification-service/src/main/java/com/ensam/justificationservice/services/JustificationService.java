package com.ensam.justificationservice.services;

import com.ensam.justificationservice.dto.JustificationDto;
import com.ensam.justificationservice.entities.Justification;
import com.ensam.justificationservice.entities.Justification.JustificationStatus;
import com.ensam.justificationservice.entities.Justification.JustificationType;
import com.ensam.justificationservice.repositories.JustificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JustificationService {

    private final JustificationRepository justificationRepository;

    @Transactional
    public JustificationDto submitJustification(JustificationDto dto, String submittedBy) {
        // Vérifier si une justification existe déjà pour cette absence
        if (justificationRepository.existsByAbsenceId(dto.getAbsenceId())) {
            throw new IllegalStateException("Justification already exists for absence ID: " + dto.getAbsenceId());
        }

        Justification justification = mapToEntity(dto);
        justification.setStatus(JustificationStatus.PENDING);
        justification.setSubmittedBy(submittedBy);

        Justification saved = justificationRepository.save(justification);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<JustificationDto> getAllJustifications() {
        return justificationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JustificationDto getJustificationById(Long id) {
        Justification justification = justificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Justification not found with id: " + id));
        return mapToDto(justification);
    }

    @Transactional(readOnly = true)
    public JustificationDto getJustificationByAbsenceId(Long absenceId) {
        Justification justification = justificationRepository.findByAbsenceId(absenceId)
                .orElseThrow(() -> new IllegalArgumentException("Justification not found for absence ID: " + absenceId));
        return mapToDto(justification);
    }

    @Transactional(readOnly = true)
    public List<JustificationDto> getJustificationsByStudentId(String studentId) {
        return justificationRepository.findByStudentIdOrderBySubmittedAtDesc(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JustificationDto> getJustificationsByStatus(JustificationStatus status) {
        return justificationRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JustificationDto> getJustificationsByType(JustificationType type) {
        return justificationRepository.findByType(type).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getPendingCount(String studentId) {
        return justificationRepository.countByStudentIdAndStatus(studentId, JustificationStatus.PENDING);
    }

    @Transactional
    public JustificationDto approveJustification(Long id, String reviewedBy) {
        Justification justification = justificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Justification not found with id: " + id));

        if (justification.getStatus() != JustificationStatus.PENDING) {
            throw new IllegalStateException("Justification has already been reviewed");
        }

        justification.setStatus(JustificationStatus.APPROVED);
        justification.setReviewedBy(reviewedBy);
        justification.setReviewedAt(LocalDateTime.now());
        justification.setRejectionReason(null);

        Justification updated = justificationRepository.save(justification);
        return mapToDto(updated);
    }

    @Transactional
    public JustificationDto rejectJustification(Long id, String reason, String reviewedBy) {
        Justification justification = justificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Justification not found with id: " + id));

        if (justification.getStatus() != JustificationStatus.PENDING) {
            throw new IllegalStateException("Justification has already been reviewed");
        }

        justification.setStatus(JustificationStatus.REJECTED);
        justification.setRejectionReason(reason);
        justification.setReviewedBy(reviewedBy);
        justification.setReviewedAt(LocalDateTime.now());

        Justification updated = justificationRepository.save(justification);
        return mapToDto(updated);
    }

    @Transactional
    public JustificationDto updateJustification(Long id, JustificationDto dto) {
        Justification justification = justificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Justification not found with id: " + id));

        // Only allow updates if status is PENDING
        if (justification.getStatus() != JustificationStatus.PENDING) {
            throw new IllegalStateException("Cannot update justification that has been reviewed");
        }

        justification.setType(dto.getType());
        justification.setDescription(dto.getDescription());
        justification.setDocumentUrl(dto.getDocumentUrl());
        justification.setDocumentName(dto.getDocumentName());

        Justification updated = justificationRepository.save(justification);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteJustification(Long id) {
        if (!justificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Justification not found with id: " + id);
        }
        justificationRepository.deleteById(id);
    }

    // Mapping methods
    private JustificationDto mapToDto(Justification entity) {
        JustificationDto dto = new JustificationDto();
        dto.setId(entity.getId());
        dto.setAbsenceId(entity.getAbsenceId());
        dto.setStudentId(entity.getStudentId());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setDocumentUrl(entity.getDocumentUrl());
        dto.setDocumentName(entity.getDocumentName());
        dto.setStatus(entity.getStatus());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setReviewedBy(entity.getReviewedBy());
        dto.setReviewedAt(entity.getReviewedAt());
        dto.setSubmittedBy(entity.getSubmittedBy());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private Justification mapToEntity(JustificationDto dto) {
        Justification entity = new Justification();
        entity.setAbsenceId(dto.getAbsenceId());
        entity.setStudentId(dto.getStudentId());
        entity.setType(dto.getType());
        entity.setDescription(dto.getDescription());
        entity.setDocumentUrl(dto.getDocumentUrl());
        entity.setDocumentName(dto.getDocumentName());
        return entity;
    }
}
