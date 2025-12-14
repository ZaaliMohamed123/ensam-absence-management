package com.ensam.absenceservice.services;

import com.ensam.absenceservice.dto.AbsenceDto;
import com.ensam.absenceservice.dto.AbsenceStatsDto;
import com.ensam.absenceservice.dto.ModuleAbsenceStatsDto;
import com.ensam.absenceservice.entities.Absence;
import com.ensam.absenceservice.entities.Absence.AbsenceStatus;
import com.ensam.absenceservice.repositories.AbsenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private static final int ABSENCE_RISK_THRESHOLD = 3; // Seuil d'alerte

    @Transactional
    public AbsenceDto createAbsence(AbsenceDto dto) {
        // Vérifier si l'absence existe déjà
        if (absenceRepository.findByStudentIdAndScheduleIdAndAbsenceDate(
                dto.getStudentId(),
                dto.getScheduleId(),
                dto.getAbsenceDate()).isPresent()) {
            throw new IllegalStateException("Absence already exists for this student, schedule and date");
        }

        Absence absence = mapToEntity(dto);
        absence.setStatus(AbsenceStatus.PENDING); // Statut par défaut
        absence.setNotified(false);

        Absence saved = absenceRepository.save(absence);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getAllAbsences() {
        return absenceRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AbsenceDto getAbsenceById(Long id) {
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Absence not found with id: " + id));
        return mapToDto(absence);
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getAbsencesByStudentId(String studentId) {
        return absenceRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getAbsencesByTeacherId(String teacherId) {
        return absenceRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getAbsencesByClassId(Long classId) {
        return absenceRepository.findByClassId(classId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getAbsencesByModuleId(Long moduleId) {
        return absenceRepository.findByModuleId(moduleId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getAbsencesByStatus(AbsenceStatus status) {
        return absenceRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AbsenceDto> getAbsencesByStudentAndDateRange(
            String studentId,
            LocalDate startDate,
            LocalDate endDate) {
        return absenceRepository.findByStudentIdAndDateRange(studentId, startDate, endDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AbsenceDto updateAbsenceStatus(Long id, AbsenceStatus status, String notes) {
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Absence not found with id: " + id));

        absence.setStatus(status);
        if (notes != null && !notes.isEmpty()) {
            absence.setNotes(notes);
        }

        Absence updated = absenceRepository.save(absence);
        return mapToDto(updated);
    }

    @Transactional
    public AbsenceDto updateAbsence(Long id, AbsenceDto dto) {
        Absence existingAbsence = absenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Absence not found with id: " + id));

        existingAbsence.setAbsenceDate(dto.getAbsenceDate());
        existingAbsence.setStartTime(dto.getStartTime());
        existingAbsence.setEndTime(dto.getEndTime());
        existingAbsence.setStatus(dto.getStatus());
        existingAbsence.setNotes(dto.getNotes());
        existingAbsence.setNotified(dto.getNotified());

        Absence updated = absenceRepository.save(existingAbsence);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteAbsence(Long id) {
        if (!absenceRepository.existsById(id)) {
            throw new IllegalArgumentException("Absence not found with id: " + id);
        }
        absenceRepository.deleteById(id);
    }

    // Statistics methods
    @Transactional(readOnly = true)
    public AbsenceStatsDto getStudentAbsenceStats(String studentId) {
        Long total = absenceRepository.countByStudentId(studentId);
        Long justified = absenceRepository.countByStudentIdAndStatus(studentId, AbsenceStatus.JUSTIFIED);
        Long unjustified = absenceRepository.countByStudentIdAndStatus(studentId, AbsenceStatus.UNJUSTIFIED);
        Long pending = absenceRepository.countByStudentIdAndStatus(studentId, AbsenceStatus.PENDING);

        // Calculer le taux d'absence (à adapter selon le nombre total de cours)
        Double absenceRate = total > 0 ? (unjustified.doubleValue() / total.doubleValue()) * 100 : 0.0;

        return new AbsenceStatsDto(total, justified, unjustified, pending, absenceRate);
    }

    @Transactional(readOnly = true)
    public List<ModuleAbsenceStatsDto> getStudentModuleAbsenceStats(String studentId) {
        List<Absence> absences = absenceRepository.findByStudentId(studentId);

        return absences.stream()
                .collect(Collectors.groupingBy(Absence::getModuleId))
                .entrySet().stream()
                .map(entry -> {
                    Long moduleId = entry.getKey();
                    List<Absence> moduleAbsences = entry.getValue();

                    Long total = (long) moduleAbsences.size();
                    Long unjustified = moduleAbsences.stream()
                            .filter(a -> a.getStatus() == AbsenceStatus.UNJUSTIFIED)
                            .count();

                    Boolean isAtRisk = unjustified >= ABSENCE_RISK_THRESHOLD;

                    return new ModuleAbsenceStatsDto(
                            moduleId,
                            "Module " + moduleId, // À remplacer par un appel au service Academic
                            total,
                            unjustified,
                            isAtRisk
                    );
                })
                .collect(Collectors.toList());
    }

    // Mapping methods
    private AbsenceDto mapToDto(Absence entity) {
        AbsenceDto dto = new AbsenceDto();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudentId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setScheduleId(entity.getScheduleId());
        dto.setModuleId(entity.getModuleId());
        dto.setClassId(entity.getClassId());
        dto.setAbsenceDate(entity.getAbsenceDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setStatus(entity.getStatus());
        dto.setNotes(entity.getNotes());
        dto.setNotified(entity.getNotified());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private Absence mapToEntity(AbsenceDto dto) {
        Absence entity = new Absence();
        entity.setStudentId(dto.getStudentId());
        entity.setTeacherId(dto.getTeacherId());
        entity.setScheduleId(dto.getScheduleId());
        entity.setModuleId(dto.getModuleId());
        entity.setClassId(dto.getClassId());
        entity.setAbsenceDate(dto.getAbsenceDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setStatus(dto.getStatus());
        entity.setNotes(dto.getNotes());
        entity.setNotified(dto.getNotified() != null ? dto.getNotified() : false);
        return entity;
    }
}
