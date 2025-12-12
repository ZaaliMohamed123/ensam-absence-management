package com.ensam.userservice.services;

import com.ensam.userservice.dto.UpdateProfileRequest;
import com.ensam.userservice.dto.UserDTO;
import com.ensam.userservice.entities.*;
import com.ensam.userservice.exception.ResourceNotFoundException;
import com.ensam.userservice.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AdminRepository adminRepository;

    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        // Update teacher department if applicable
        if (user.getRole() == Role.ROLE_TEACHER && request.getDepartment() != null) {
            Teacher teacher = teacherRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));
            teacher.setDepartment(request.getDepartment());
            teacherRepository.save(teacher);
        }

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    public List<UserDTO> getAllStudents() {
        return userRepository.findByRole(Role.ROLE_STUDENT)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllTeachers() {
        return userRepository.findByRole(Role.ROLE_TEACHER)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());

        // Add role-specific data
        switch (user.getRole()) {
            case ROLE_STUDENT:
                studentRepository.findByUserId(user.getId()).ifPresent(student -> {
                    dto.setStudentNumber(student.getStudentNumber());
                    dto.setClassId(student.getClassId());
                    dto.setEnrollmentYear(student.getEnrollmentYear());
                });
                break;
            case ROLE_TEACHER:
                teacherRepository.findByUserId(user.getId()).ifPresent(teacher -> {
                    dto.setTeacherCode(teacher.getTeacherCode());
                    dto.setDepartment(teacher.getDepartment());
                });
                break;
            case ROLE_ADMIN:
                adminRepository.findByUserId(user.getId()).ifPresent(admin -> {
                    dto.setAdminCode(admin.getAdminCode());
                });
                break;
        }

        return dto;
    }
}
