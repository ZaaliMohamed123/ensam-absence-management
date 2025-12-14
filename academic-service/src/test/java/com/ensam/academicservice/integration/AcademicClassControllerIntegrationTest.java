package com.ensam.academicservice.integration;

import com.ensam.academicservice.dto.AcademicClassDto;
import com.ensam.academicservice.repositories.AcademicClassRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Academic Class Controller Integration Tests")
class AcademicClassControllerIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AcademicClassRepository classRepository;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @Autowired
    public AcademicClassControllerIntegrationTest(
            MockMvc mockMvc,
            AcademicClassRepository classRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // ✅ Ajouter cette ligne
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ✅ Optionnel mais recommandé
        this.classRepository = classRepository;
    }

    @BeforeEach
    void setUp() {
        classRepository.deleteAll();

        // Generate tokens
        adminToken = generateToken("admin@ensam.ma", "ROLE_ADMIN");
        teacherToken = generateToken("teacher@ensam.ma", "ROLE_TEACHER");
        studentToken = generateToken("student@ensam.ma", "ROLE_STUDENT");
    }

    @Test
    @DisplayName("Admin should create class successfully")
    void testCreateClassAsAdmin() throws Exception {
        AcademicClassDto dto = new AcademicClassDto();
        dto.setClassName("GI-2A-1");
        dto.setLevel("2A");
        dto.setBranch("Génie Informatique");
        dto.setAcademicYear(2025);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.className").value("GI-2A-1"))
                .andExpect(jsonPath("$.level").value("2A"))
                .andExpect(jsonPath("$.branch").value("Génie Informatique"))
                .andExpect(jsonPath("$.academicYear").value(2025));
    }

    @Test
    @DisplayName("Should fail to create class without authentication")
    void testCreateClassWithoutAuth() throws Exception {
        AcademicClassDto dto = new AcademicClassDto();
        dto.setClassName("GI-2A-1");
        dto.setLevel("2A");
        dto.setBranch("Génie Informatique");
        dto.setAcademicYear(2025);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Teacher should not create class")
    void testCreateClassAsTeacher() throws Exception {
        AcademicClassDto dto = new AcademicClassDto();
        dto.setClassName("GI-2A-1");
        dto.setLevel("2A");
        dto.setBranch("Génie Informatique");
        dto.setAcademicYear(2025);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/classes")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to create class with duplicate name")
    void testCreateClassDuplicateName() throws Exception {
        // Create first class
        AcademicClassDto dto1 = new AcademicClassDto();
        dto1.setClassName("GI-2A-1");
        dto1.setLevel("2A");
        dto1.setBranch("Génie Informatique");
        dto1.setAcademicYear(2025);

        String requestJson1 = objectMapper.writeValueAsString(dto1);

        mockMvc.perform(post("/api/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson1))
                .andExpect(status().isCreated());

        // Try to create duplicate
        AcademicClassDto dto2 = new AcademicClassDto();
        dto2.setClassName("GI-2A-1");
        dto2.setLevel("2A");
        dto2.setBranch("Génie Informatique");
        dto2.setAcademicYear(2025);

        String requestJson2 = objectMapper.writeValueAsString(dto2);

        mockMvc.perform(post("/api/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Class name already exists"));
    }

    @Test
    @DisplayName("Should fail to create class with missing fields")
    void testCreateClassMissingFields() throws Exception {
        AcademicClassDto dto = new AcademicClassDto();
        dto.setClassName(""); // Empty class name

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin and Teacher should get all classes")
    void testGetAllClassesAsAdminAndTeacher() throws Exception {
        // Create test classes
        createTestClass("GI-2A-1", "2A", "Génie Informatique");
        createTestClass("GI-3A-1", "3A", "Génie Informatique");

        // Test as Admin
        mockMvc.perform(get("/api/classes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].className").exists())
                .andExpect(jsonPath("$[1].className").exists());

        // Test as Teacher
        mockMvc.perform(get("/api/classes")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Student should not get all classes")
    void testGetAllClassesAsStudent() throws Exception {
        mockMvc.perform(get("/api/classes")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("All roles should get class by ID")
    void testGetClassById() throws Exception {
        // Create a class
        AcademicClassDto created = createTestClass("GI-2A-1", "2A", "Génie Informatique");

        // Test as Admin
        mockMvc.perform(get("/api/classes/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.className").value("GI-2A-1"));

        // Test as Teacher
        mockMvc.perform(get("/api/classes/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.className").value("GI-2A-1"));

        // Test as Student
        mockMvc.perform(get("/api/classes/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.className").value("GI-2A-1"));
    }

    @Test
    @DisplayName("Should return 400 when class not found")
    void testGetClassByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/classes/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Class not found with id: 999"));
    }

    @Test
    @DisplayName("Should get classes by level")
    void testGetClassesByLevel() throws Exception {
        createTestClass("GI-2A-1", "2A", "Génie Informatique");
        createTestClass("GI-2A-2", "2A", "Génie Informatique");
        createTestClass("GI-3A-1", "3A", "Génie Informatique");

        mockMvc.perform(get("/api/classes/level/2A")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].level", everyItem(is("2A"))));
    }

    @Test
    @DisplayName("Should get classes by branch")
    void testGetClassesByBranch() throws Exception {
        createTestClass("GI-2A-1", "2A", "Génie Informatique");
        createTestClass("GC-2A-1", "2A", "Génie Civil");

        mockMvc.perform(get("/api/classes/branch/Génie Informatique")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].branch").value("Génie Informatique"));
    }

    @Test
    @DisplayName("Admin should update class successfully")
    void testUpdateClass() throws Exception {
        AcademicClassDto created = createTestClass("GI-2A-1", "2A", "Génie Informatique");

        AcademicClassDto updateDto = new AcademicClassDto();
        updateDto.setClassName("GI-2A-1-UPDATED");
        updateDto.setLevel("2A");
        updateDto.setBranch("Génie Informatique");
        updateDto.setAcademicYear(2025);

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/classes/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.className").value("GI-2A-1-UPDATED"));
    }

    @Test
    @DisplayName("Teacher should not update class")
    void testUpdateClassAsTeacher() throws Exception {
        AcademicClassDto created = createTestClass("GI-2A-1", "2A", "Génie Informatique");

        AcademicClassDto updateDto = new AcademicClassDto();
        updateDto.setClassName("GI-2A-1-UPDATED");
        updateDto.setLevel("2A");
        updateDto.setBranch("Génie Informatique");
        updateDto.setAcademicYear(2025);

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/classes/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should delete class successfully")
    void testDeleteClass() throws Exception {
        AcademicClassDto created = createTestClass("GI-2A-1", "2A", "Génie Informatique");

        mockMvc.perform(delete("/api/classes/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/classes/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Student should not delete class")
    void testDeleteClassAsStudent() throws Exception {
        AcademicClassDto created = createTestClass("GI-2A-1", "2A", "Génie Informatique");

        mockMvc.perform(delete("/api/classes/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    // Helper method to create test class
    private AcademicClassDto createTestClass(String className, String level, String branch) throws Exception {
        AcademicClassDto dto = new AcademicClassDto();
        dto.setClassName(className);
        dto.setLevel(level);
        dto.setBranch(branch);
        dto.setAcademicYear(2025);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, AcademicClassDto.class);
    }
}
