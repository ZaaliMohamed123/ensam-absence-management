package com.ensam.userservice.repositories;

import com.ensam.userservice.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByStudentNumber(String studentNumber);
    List<Student> findByClassId(Long classId);
    boolean existsByStudentNumber(String studentNumber);
}
