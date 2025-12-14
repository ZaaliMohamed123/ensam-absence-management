package com.ensam.academicservice.services;

import com.ensam.academicservice.dto.ModuleDto;
import com.ensam.academicservice.entities.AcademicClass;
import com.ensam.academicservice.entities.Module;
import com.ensam.academicservice.repositories.AcademicClassRepository;
import com.ensam.academicservice.repositories.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final AcademicClassRepository classRepository;

    @Transactional
    public ModuleDto createModule(ModuleDto dto) {
        if (moduleRepository.existsByModuleCode(dto.getModuleCode())) {
            throw new IllegalStateException("Module code already exists");
        }

        AcademicClass academicClass = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + dto.getClassId()));

        Module module = mapToEntity(dto, academicClass);
        Module saved = moduleRepository.save(module);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ModuleDto> getAllModules() {
        return moduleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ModuleDto getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + id));
        return mapToDto(module);
    }

    @Transactional(readOnly = true)
    public List<ModuleDto> getModulesByClassId(Long classId) {
        return moduleRepository.findByAcademicClassId(classId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ModuleDto updateModule(Long id, ModuleDto dto) {
        Module existingModule = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + id));

        if (!existingModule.getModuleCode().equals(dto.getModuleCode()) &&
                moduleRepository.existsByModuleCode(dto.getModuleCode())) {
            throw new IllegalStateException("Module code already exists");
        }

        AcademicClass academicClass = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + dto.getClassId()));

        existingModule.setModuleCode(dto.getModuleCode());
        existingModule.setModuleName(dto.getModuleName());
        existingModule.setDescription(dto.getDescription());
        existingModule.setCredits(dto.getCredits());
        existingModule.setTotalHours(dto.getTotalHours());
        existingModule.setAcademicClass(academicClass);

        Module updated = moduleRepository.save(existingModule);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteModule(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Module not found with id: " + id);
        }
        moduleRepository.deleteById(id);
    }

    private ModuleDto mapToDto(Module entity) {
        ModuleDto dto = new ModuleDto();
        dto.setId(entity.getId());
        dto.setModuleCode(entity.getModuleCode());
        dto.setModuleName(entity.getModuleName());
        dto.setDescription(entity.getDescription());
        dto.setCredits(entity.getCredits());
        dto.setTotalHours(entity.getTotalHours());
        dto.setClassId(entity.getAcademicClass().getId());
        dto.setClassName(entity.getAcademicClass().getClassName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private Module mapToEntity(ModuleDto dto, AcademicClass academicClass) {
        Module entity = new Module();
        entity.setModuleCode(dto.getModuleCode());
        entity.setModuleName(dto.getModuleName());
        entity.setDescription(dto.getDescription());
        entity.setCredits(dto.getCredits());
        entity.setTotalHours(dto.getTotalHours());
        entity.setAcademicClass(academicClass);
        return entity;
    }
}
