package com.ensam.academicservice.controllers;

import com.ensam.academicservice.dto.AcademicClassDto;
import com.ensam.academicservice.services.AcademicClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class AcademicClassController {

    private final AcademicClassService classService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AcademicClassDto> createClass(@Valid @RequestBody AcademicClassDto dto) {
        AcademicClassDto created = classService.createClass(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AcademicClassDto>> getAllClasses() {
        List<AcademicClassDto> classes = classService.getAllClasses();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<AcademicClassDto> getClassById(@PathVariable Long id) {
        AcademicClassDto classDto = classService.getClassById(id);
        return ResponseEntity.ok(classDto);
    }

    @GetMapping("/level/{level}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AcademicClassDto>> getClassesByLevel(@PathVariable String level) {
        List<AcademicClassDto> classes = classService.getClassesByLevel(level);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/branch/{branch}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AcademicClassDto>> getClassesByBranch(@PathVariable String branch) {
        List<AcademicClassDto> classes = classService.getClassesByBranch(branch);
        return ResponseEntity.ok(classes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AcademicClassDto> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody AcademicClassDto dto) {
        AcademicClassDto updated = classService.updateClass(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}
