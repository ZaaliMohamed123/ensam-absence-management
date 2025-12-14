package com.ensam.justificationservice.controllers;

import com.ensam.justificationservice.dto.JustificationDto;
import com.ensam.justificationservice.entities.Justification.JustificationStatus;
import com.ensam.justificationservice.entities.Justification.JustificationType;
import com.ensam.justificationservice.services.JustificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/justifications")
@RequiredArgsConstructor
public class JustificationController {

    private final JustificationService justificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<JustificationDto> submitJustification(
            @Valid @RequestBody JustificationDto dto,
            Authentication authentication) {
        String submittedBy = authentication.getName();
        JustificationDto created = justificationService.submitJustification(dto, submittedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<JustificationDto>> getAllJustifications() {
        List<JustificationDto> justifications = justificationService.getAllJustifications();
        return ResponseEntity.ok(justifications);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<JustificationDto> getJustificationById(@PathVariable Long id) {
        JustificationDto justification = justificationService.getJustificationById(id);
        return ResponseEntity.ok(justification);
    }

    @GetMapping("/absence/{absenceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<JustificationDto> getJustificationByAbsenceId(@PathVariable Long absenceId) {
        JustificationDto justification = justificationService.getJustificationByAbsenceId(absenceId);
        return ResponseEntity.ok(justification);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<JustificationDto>> getJustificationsByStudentId(@PathVariable String studentId) {
        List<JustificationDto> justifications = justificationService.getJustificationsByStudentId(studentId);
        return ResponseEntity.ok(justifications);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<JustificationDto>> getJustificationsByStatus(@PathVariable JustificationStatus status) {
        List<JustificationDto> justifications = justificationService.getJustificationsByStatus(status);
        return ResponseEntity.ok(justifications);
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<JustificationDto>> getJustificationsByType(@PathVariable JustificationType type) {
        List<JustificationDto> justifications = justificationService.getJustificationsByType(type);
        return ResponseEntity.ok(justifications);
    }

    @GetMapping("/student/{studentId}/pending/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Map<String, Long>> getPendingCount(@PathVariable String studentId) {
        Long count = justificationService.getPendingCount(studentId);
        return ResponseEntity.ok(Map.of("pendingCount", count));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<JustificationDto> approveJustification(
            @PathVariable Long id,
            Authentication authentication) {
        String reviewedBy = authentication.getName();
        JustificationDto approved = justificationService.approveJustification(id, reviewedBy);
        return ResponseEntity.ok(approved);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<JustificationDto> rejectJustification(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {
        String reviewedBy = authentication.getName();
        JustificationDto rejected = justificationService.rejectJustification(id, reason, reviewedBy);
        return ResponseEntity.ok(rejected);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<JustificationDto> updateJustification(
            @PathVariable Long id,
            @Valid @RequestBody JustificationDto dto) {
        JustificationDto updated = justificationService.updateJustification(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJustification(@PathVariable Long id) {
        justificationService.deleteJustification(id);
        return ResponseEntity.noContent().build();
    }
}
