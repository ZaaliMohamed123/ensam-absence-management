package com.ensam.academicservice.services;

import com.ensam.academicservice.dto.ScheduleDto;
import com.ensam.academicservice.entities.AcademicClass;
import com.ensam.academicservice.entities.Module;
import com.ensam.academicservice.entities.Schedule;
import com.ensam.academicservice.repositories.AcademicClassRepository;
import com.ensam.academicservice.repositories.ModuleRepository;
import com.ensam.academicservice.repositories.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final AcademicClassRepository classRepository;
    private final ModuleRepository moduleRepository;

    @Transactional
    public ScheduleDto createSchedule(ScheduleDto dto) {
        AcademicClass academicClass = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + dto.getClassId()));

        Module module = moduleRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + dto.getModuleId()));

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Schedule schedule = mapToEntity(dto, academicClass, module);
        Schedule saved = scheduleRepository.save(schedule);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScheduleDto getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + id));
        return mapToDto(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByClassId(Long classId) {
        return scheduleRepository.findByAcademicClassId(classId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByTeacherId(String teacherId) {
        return scheduleRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByClassIdAndDay(Long classId, DayOfWeek dayOfWeek) {
        return scheduleRepository.findByClassIdAndDayOfWeek(classId, dayOfWeek).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleDto updateSchedule(Long id, ScheduleDto dto) {
        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + id));

        AcademicClass academicClass = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + dto.getClassId()));

        Module module = moduleRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + dto.getModuleId()));

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        existingSchedule.setAcademicClass(academicClass);
        existingSchedule.setModule(module);
        existingSchedule.setTeacherId(dto.getTeacherId());
        existingSchedule.setDayOfWeek(dto.getDayOfWeek());
        existingSchedule.setStartTime(dto.getStartTime());
        existingSchedule.setEndTime(dto.getEndTime());
        existingSchedule.setRoom(dto.getRoom());
        existingSchedule.setSessionType(dto.getSessionType());
        existingSchedule.setIsRecurring(dto.getIsRecurring());

        Schedule updated = scheduleRepository.save(existingSchedule);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    private ScheduleDto mapToDto(Schedule entity) {
        ScheduleDto dto = new ScheduleDto();
        dto.setId(entity.getId());
        dto.setClassId(entity.getAcademicClass().getId());
        dto.setModuleId(entity.getModule().getId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setRoom(entity.getRoom());
        dto.setSessionType(entity.getSessionType());
        dto.setIsRecurring(entity.getIsRecurring());
        dto.setClassName(entity.getAcademicClass().getClassName());
        dto.setModuleName(entity.getModule().getModuleName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private Schedule mapToEntity(ScheduleDto dto, AcademicClass academicClass, Module module) {
        Schedule entity = new Schedule();
        entity.setAcademicClass(academicClass);
        entity.setModule(module);
        entity.setTeacherId(dto.getTeacherId());
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setRoom(dto.getRoom());
        entity.setSessionType(dto.getSessionType());
        entity.setIsRecurring(dto.getIsRecurring());
        return entity;
    }
}
