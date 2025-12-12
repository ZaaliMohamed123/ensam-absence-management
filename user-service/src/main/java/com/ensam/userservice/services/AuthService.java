package com.ensam.userservice.services;

import com.ensam.userservice.dto.AuthResponse;
import com.ensam.userservice.dto.LoginRequest;
import com.ensam.userservice.dto.RegisterRequest;
import com.ensam.userservice.entities.*;
import com.ensam.userservice.exception.BadRequestException;
import com.ensam.userservice.repositories.AdminRepository;
import com.ensam.userservice.repositories.StudentRepository;
import com.ensam.userservice.repositories.TeacherRepository;
import com.ensam.userservice.repositories.UserRepository;
import com.ensam.userservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);

        user = userRepository.save(user);

        // Create role-specific entity
        switch (request.getRole()) {
            case ROLE_STUDENT:
                createStudent(user, request);
                break;
            case ROLE_TEACHER:
                createTeacher(user, request);
                break;
            case ROLE_ADMIN:
                createAdmin(user, request);
                break;
        }

        // Generate token
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    private void createStudent(User user, RegisterRequest request) {
        if (request.getStudentNumber() == null) {
            throw new BadRequestException("Student number is required for students");
        }

        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new BadRequestException("Student number already exists");
        }

        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber(request.getStudentNumber());
        student.setClassId(request.getClassId());
        student.setEnrollmentYear(request.getEnrollmentYear());
        studentRepository.save(student);
    }

    private void createTeacher(User user, RegisterRequest request) {
        if (request.getTeacherCode() == null) {
            throw new BadRequestException("Teacher code is required for teachers");
        }

        if (teacherRepository.existsByTeacherCode(request.getTeacherCode())) {
            throw new BadRequestException("Teacher code already exists");
        }

        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setTeacherCode(request.getTeacherCode());
        teacher.setDepartment(request.getDepartment());
        teacherRepository.save(teacher);
    }

    private void createAdmin(User user, RegisterRequest request) {
        if (request.getAdminCode() == null) {
            throw new BadRequestException("Admin code is required for admins");
        }

        if (adminRepository.existsByAdminCode(request.getAdminCode())) {
            throw new BadRequestException("Admin code already exists");
        }

        Admin admin = new Admin();
        admin.setUser(user);
        admin.setAdminCode(request.getAdminCode());
        adminRepository.save(admin);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadRequestException("User account is inactive");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
