package com.ensam.academicservice.repositories;

import com.ensam.academicservice.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByModuleCode(String moduleCode);
    List<Module> findByAcademicClassId(Long classId);
    boolean existsByModuleCode(String moduleCode);
}
