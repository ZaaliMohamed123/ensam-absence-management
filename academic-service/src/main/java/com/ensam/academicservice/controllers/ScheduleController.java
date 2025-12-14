package com.ensam.academicservice.controllers;

import com.ensam.academicservice.dto.ScheduleDto;
import com.ensam.academicservice.services.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleDto> createSchedule(@Valid @RequestBody ScheduleDto dto) {
        ScheduleDto created = scheduleService.createSchedule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ScheduleDto>> getAllSchedules() {
        List<ScheduleDto> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable Long id) {
        ScheduleDto schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByClassId(@PathVariable Long classId) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByClassId(classId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByTeacherId(@PathVariable String teacherId) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByTeacherId(teacherId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/class/{classId}/day/{dayOfWeek}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByClassIdAndDay(
            @PathVariable Long classId,
            @PathVariable DayOfWeek dayOfWeek) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByClassIdAndDay(classId, dayOfWeek);
        return ResponseEntity.ok(schedules);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleDto> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleDto dto) {
        ScheduleDto updated = scheduleService.updateSchedule(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
