package com.ensam.userservice.integration;

import com.ensam.userservice.dto.LoginRequest;
import com.ensam.userservice.dto.RegisterRequest;
import com.ensam.userservice.entities.Role;
import com.ensam.userservice.repositories.AdminRepository;
import com.ensam.userservice.repositories.StudentRepository;
import com.ensam.userservice.repositories.TeacherRepository;
import com.ensam.userservice.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserRepository userRepository;
    private StudentRepository studentRepository;
    private TeacherRepository teacherRepository;
    private AdminRepository adminRepository;

    @Autowired
    public AuthControllerIntegrationTest(
            MockMvc mockMvc,
            UserRepository userRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            AdminRepository adminRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.adminRepository = adminRepository;
    }

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();
        teacherRepository.deleteAll();
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register admin successfully")
    void testRegisterAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@ensam.ma");
        request.setPassword("Admin123!");
        request.setRole(Role.ROLE_ADMIN);
        request.setFirstName("Ahmed");
        request.setLastName("Alaoui");
        request.setAdminCode("ADM001");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("admin@ensam.ma"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.lastName").value("Alaoui"));
    }

    @Test
    @DisplayName("Should register teacher successfully")
    void testRegisterTeacher() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("teacher@ensam.ma");
        request.setPassword("Teacher123!");
        request.setRole(Role.ROLE_TEACHER);
        request.setFirstName("Fatima");
        request.setLastName("El Mansouri");
        request.setTeacherCode("TCH001");
        request.setDepartment("Informatique");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ROLE_TEACHER"))
                .andExpect(jsonPath("$.email").value("teacher@ensam.ma"));
    }

    @Test
    @DisplayName("Should register student successfully")
    void testRegisterStudent() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("student@ensam.ma");
        request.setPassword("Student123!");
        request.setRole(Role.ROLE_STUDENT);
        request.setFirstName("Youssef");
        request.setLastName("Bennani");
        request.setStudentNumber("STU2025001");
        request.setClassId(1L);
        request.setEnrollmentYear(2025);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.email").value("student@ensam.ma"));
    }

    @Test
    @DisplayName("Should fail to register with duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest firstRequest = new RegisterRequest();
        firstRequest.setEmail("duplicate@ensam.ma");
        firstRequest.setPassword("Test123!");
        firstRequest.setRole(Role.ROLE_ADMIN);
        firstRequest.setFirstName("First");
        firstRequest.setLastName("User");
        firstRequest.setAdminCode("ADM001");

        String firstRequestJson = objectMapper.writeValueAsString(firstRequest);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequestJson))
                .andExpect(status().isOk());

        RegisterRequest secondRequest = new RegisterRequest();
        secondRequest.setEmail("duplicate@ensam.ma");
        secondRequest.setPassword("Test456!");
        secondRequest.setRole(Role.ROLE_ADMIN);
        secondRequest.setFirstName("Second");
        secondRequest.setLastName("User");
        secondRequest.setAdminCode("ADM002");

        String secondRequestJson = objectMapper.writeValueAsString(secondRequest);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @Test
    @DisplayName("Should fail to register student without student number")
    void testRegisterStudentWithoutStudentNumber() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("student@ensam.ma");
        request.setPassword("Student123!");
        request.setRole(Role.ROLE_STUDENT);
        request.setFirstName("Youssef");
        request.setLastName("Bennani");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Student number is required for students"));
    }

    @Test
    @DisplayName("Should fail to register with invalid email")
    void testRegisterInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("Test123!");
        request.setRole(Role.ROLE_STUDENT);
        request.setFirstName("Test");
        request.setLastName("User");
        request.setStudentNumber("STU999");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email should be valid"));
    }

    @Test
    @DisplayName("Should login successfully")
    void testLoginSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("login@ensam.ma");
        registerRequest.setPassword("Login123!");
        registerRequest.setRole(Role.ROLE_ADMIN);
        registerRequest.setFirstName("Login");
        registerRequest.setLastName("Test");
        registerRequest.setAdminCode("ADM001");

        String registerJson = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@ensam.ma");
        loginRequest.setPassword("Login123!");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("login@ensam.ma"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should fail to login with wrong password")
    void testLoginWrongPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("wrongpass@ensam.ma");
        registerRequest.setPassword("Correct123!");
        registerRequest.setRole(Role.ROLE_ADMIN);
        registerRequest.setFirstName("Wrong");
        registerRequest.setLastName("Pass");
        registerRequest.setAdminCode("ADM001");

        String registerJson = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrongpass@ensam.ma");
        loginRequest.setPassword("WrongPassword!");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should fail to login with non-existent email")
    void testLoginNonExistentEmail() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@ensam.ma");
        loginRequest.setPassword("Password123!");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
