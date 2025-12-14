package com.ensam.academicservice.services;

import com.ensam.academicservice.dto.AcademicClassDto;
import com.ensam.academicservice.entities.AcademicClass;
import com.ensam.academicservice.repositories.AcademicClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicClassService {

    private final AcademicClassRepository classRepository;

    @Transactional
    public AcademicClassDto createClass(AcademicClassDto dto) {
        if (classRepository.existsByClassName(dto.getClassName())) {
            throw new IllegalStateException("Class name already exists");
        }

        AcademicClass academicClass = mapToEntity(dto);
        AcademicClass saved = classRepository.save(academicClass);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AcademicClassDto> getAllClasses() {
        return classRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademicClassDto getClassById(Long id) {
        AcademicClass academicClass = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + id));
        return mapToDto(academicClass);
    }

    @Transactional(readOnly = true)
    public List<AcademicClassDto> getClassesByLevel(String level) {
        return classRepository.findByLevel(level).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AcademicClassDto> getClassesByBranch(String branch) {
        return classRepository.findByBranch(branch).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AcademicClassDto updateClass(Long id, AcademicClassDto dto) {
        AcademicClass existingClass = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + id));

        if (!existingClass.getClassName().equals(dto.getClassName()) &&
                classRepository.existsByClassName(dto.getClassName())) {
            throw new IllegalStateException("Class name already exists");
        }

        existingClass.setClassName(dto.getClassName());
        existingClass.setLevel(dto.getLevel());
        existingClass.setBranch(dto.getBranch());
        existingClass.setAcademicYear(dto.getAcademicYear());
        existingClass.setDelegateId(dto.getDelegateId());

        AcademicClass updated = classRepository.save(existingClass);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteClass(Long id) {
        if (!classRepository.existsById(id)) {
            throw new IllegalArgumentException("Class not found with id: " + id);
        }
        classRepository.deleteById(id);
    }

    private AcademicClassDto mapToDto(AcademicClass entity) {
        AcademicClassDto dto = new AcademicClassDto();
        dto.setId(entity.getId());
        dto.setClassName(entity.getClassName());
        dto.setLevel(entity.getLevel());
        dto.setBranch(entity.getBranch());
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setDelegateId(entity.getDelegateId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private AcademicClass mapToEntity(AcademicClassDto dto) {
        AcademicClass entity = new AcademicClass();
        entity.setClassName(dto.getClassName());
        entity.setLevel(dto.getLevel());
        entity.setBranch(dto.getBranch());
        entity.setAcademicYear(dto.getAcademicYear());
        entity.setDelegateId(dto.getDelegateId());
        return entity;
    }
}
