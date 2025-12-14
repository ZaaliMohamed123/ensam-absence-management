package com.ensam.academicservice.repositories;

import com.ensam.academicservice.entities.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicClassRepository extends JpaRepository<AcademicClass, Long> {
    Optional<AcademicClass> findByClassName(String className);
    List<AcademicClass> findByLevel(String level);
    List<AcademicClass> findByBranch(String branch);
    List<AcademicClass> findByAcademicYear(Integer academicYear);
    boolean existsByClassName(String className);
}
