package com.ensam.absenceservice.controllers;

import com.ensam.absenceservice.dto.AbsenceDto;
import com.ensam.absenceservice.dto.AbsenceStatsDto;
import com.ensam.absenceservice.dto.ModuleAbsenceStatsDto;
import com.ensam.absenceservice.entities.Absence.AbsenceStatus;
import com.ensam.absenceservice.services.AbsenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceService absenceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AbsenceDto> createAbsence(@Valid @RequestBody AbsenceDto dto) {
        AbsenceDto created = absenceService.createAbsence(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AbsenceDto>> getAllAbsences() {
        List<AbsenceDto> absences = absenceService.getAllAbsences();
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<AbsenceDto> getAbsenceById(@PathVariable Long id) {
        AbsenceDto absence = absenceService.getAbsenceById(id);
        return ResponseEntity.ok(absence);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<AbsenceDto>> getAbsencesByStudentId(@PathVariable String studentId) {
        List<AbsenceDto> absences = absenceService.getAbsencesByStudentId(studentId);
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AbsenceDto>> getAbsencesByTeacherId(@PathVariable String teacherId) {
        List<AbsenceDto> absences = absenceService.getAbsencesByTeacherId(teacherId);
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AbsenceDto>> getAbsencesByClassId(@PathVariable Long classId) {
        List<AbsenceDto> absences = absenceService.getAbsencesByClassId(classId);
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/module/{moduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AbsenceDto>> getAbsencesByModuleId(@PathVariable Long moduleId) {
        List<AbsenceDto> absences = absenceService.getAbsencesByModuleId(moduleId);
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AbsenceDto>> getAbsencesByStatus(@PathVariable AbsenceStatus status) {
        List<AbsenceDto> absences = absenceService.getAbsencesByStatus(status);
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/student/{studentId}/daterange")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<AbsenceDto>> getAbsencesByStudentAndDateRange(
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AbsenceDto> absences = absenceService.getAbsencesByStudentAndDateRange(studentId, startDate, endDate);
        return ResponseEntity.ok(absences);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AbsenceDto> updateAbsenceStatus(
            @PathVariable Long id,
            @RequestParam AbsenceStatus status,
            @RequestParam(required = false) String notes) {
        AbsenceDto updated = absenceService.updateAbsenceStatus(id, status, notes);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<AbsenceDto> updateAbsence(
            @PathVariable Long id,
            @Valid @RequestBody AbsenceDto dto) {
        AbsenceDto updated = absenceService.updateAbsence(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAbsence(@PathVariable Long id) {
        absenceService.deleteAbsence(id);
        return ResponseEntity.noContent().build();
    }

    // Statistics endpoints
    @GetMapping("/stats/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<AbsenceStatsDto> getStudentAbsenceStats(@PathVariable String studentId) {
        AbsenceStatsDto stats = absenceService.getStudentAbsenceStats(studentId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/student/{studentId}/modules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ModuleAbsenceStatsDto>> getStudentModuleAbsenceStats(@PathVariable String studentId) {
        List<ModuleAbsenceStatsDto> stats = absenceService.getStudentModuleAbsenceStats(studentId);
        return ResponseEntity.ok(stats);
    }
}
