package com.ensam.academicservice.controllers;

import com.ensam.academicservice.dto.ModuleDto;
import com.ensam.academicservice.services.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleDto> createModule(@Valid @RequestBody ModuleDto dto) {
        ModuleDto created = moduleService.createModule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ModuleDto>> getAllModules() {
        List<ModuleDto> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ModuleDto> getModuleById(@PathVariable Long id) {
        ModuleDto module = moduleService.getModuleById(id);
        return ResponseEntity.ok(module);
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ModuleDto>> getModulesByClassId(@PathVariable Long classId) {
        List<ModuleDto> modules = moduleService.getModulesByClassId(classId);
        return ResponseEntity.ok(modules);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleDto> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody ModuleDto dto) {
        ModuleDto updated = moduleService.updateModule(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
}
