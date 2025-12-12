package com.ensam.userservice.integration;

import com.ensam.userservice.dto.RegisterRequest;
import com.ensam.userservice.dto.UpdateProfileRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserRepository userRepository;
    private StudentRepository studentRepository;
    private TeacherRepository teacherRepository;
    private AdminRepository adminRepository;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @Autowired
    public UserControllerIntegrationTest(
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
    void setUp() throws Exception {
        adminRepository.deleteAll();
        teacherRepository.deleteAll();
        studentRepository.deleteAll();
        userRepository.deleteAll();

        adminToken = registerUser("admin@ensam.ma", Role.ROLE_ADMIN, "ADM001", null, null);
        teacherToken = registerUser("teacher@ensam.ma", Role.ROLE_TEACHER, null, "TCH001", null);
        studentToken = registerUser("student@ensam.ma", Role.ROLE_STUDENT, null, null, "STU2025001");

        registerUser("student2@ensam.ma", Role.ROLE_STUDENT, null, null, "STU2025002");
        registerUser("student3@ensam.ma", Role.ROLE_STUDENT, null, null, "STU2025003");
    }

    private String registerUser(String email, Role role, String adminCode, String teacherCode, String studentNumber) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("Password123!");
        request.setRole(role);
        request.setFirstName("Test");
        request.setLastName("User");

        if (adminCode != null) request.setAdminCode(adminCode);
        if (teacherCode != null) {
            request.setTeacherCode(teacherCode);
            request.setDepartment("Informatique");
        }
        if (studentNumber != null) {
            request.setStudentNumber(studentNumber);
            request.setClassId(1L);
            request.setEnrollmentYear(2025);
        }

        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("Should get admin profile successfully")
    void testGetAdminProfile() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@ensam.ma"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.adminCode").value("ADM001"));
    }

    @Test
    @DisplayName("Should get teacher profile successfully")
    void testGetTeacherProfile() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("teacher@ensam.ma"))
                .andExpect(jsonPath("$.role").value("ROLE_TEACHER"))
                .andExpect(jsonPath("$.teacherCode").value("TCH001"))
                .andExpect(jsonPath("$.department").value("Informatique"));
    }

    @Test
    @DisplayName("Should get student profile successfully")
    void testGetStudentProfile() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@ensam.ma"))
                .andExpect(jsonPath("$.role").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.studentNumber").value("STU2025001"))
                .andExpect(jsonPath("$.classId").value(1))
                .andExpect(jsonPath("$.enrollmentYear").value(2025));
    }

    @Test
    @DisplayName("Should fail to get profile without token")
    void testGetProfileWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should update student profile successfully")
    void testUpdateProfile() throws Exception {
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");

        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }

    @Test
    @DisplayName("Admin should get all students successfully")
    void testGetAllStudents() throws Exception {
        mockMvc.perform(get("/api/users/students")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].role", everyItem(is("ROLE_STUDENT"))));
    }

    @Test
    @DisplayName("Admin should get all teachers successfully")
    void testGetAllTeachers() throws Exception {
        mockMvc.perform(get("/api/users/teachers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("ROLE_TEACHER"))
                .andExpect(jsonPath("$[0].email").value("teacher@ensam.ma"));
    }

    @Test
    @DisplayName("Admin should get all users successfully")
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    @DisplayName("Admin should get user by ID successfully")
    void testGetUserById() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andReturn();

        Long userId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("student@ensam.ma"));
    }

    @Test
    @DisplayName("Student should not access admin endpoints")
    void testStudentAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/users/students")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Teacher should not access admin endpoints")
    void testTeacherAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/users/all")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to get user by ID with student token")
    void testGetUserByIdAsStudent() throws Exception {
        mockMvc.perform(get("/api/users/1")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
