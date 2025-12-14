package com.ensam.academicservice.integration;

import com.ensam.academicservice.dto.AcademicClassDto;
import com.ensam.academicservice.dto.ModuleDto;
import com.ensam.academicservice.repositories.AcademicClassRepository;
import com.ensam.academicservice.repositories.ModuleRepository;
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

@DisplayName("Module Controller Integration Tests")
class ModuleControllerIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ModuleRepository moduleRepository;
    private AcademicClassRepository classRepository;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @Autowired
    public ModuleControllerIntegrationTest(
            MockMvc mockMvc,
            ModuleRepository moduleRepository,
            AcademicClassRepository classRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // ✅ CETTE LIGNE
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ✅ CETTE LIGNE
        this.moduleRepository = moduleRepository;
        this.classRepository = classRepository;
    }


    @BeforeEach
    void setUp() {
        moduleRepository.deleteAll();
        classRepository.deleteAll();

        adminToken = generateToken("admin@ensam.ma", "ROLE_ADMIN");
        teacherToken = generateToken("teacher@ensam.ma", "ROLE_TEACHER");
        studentToken = generateToken("student@ensam.ma", "ROLE_STUDENT");
    }

    @Test
    @DisplayName("Admin should create module successfully")
    void testCreateModuleAsAdmin() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");

        ModuleDto moduleDto = new ModuleDto();
        moduleDto.setModuleCode("INF201");
        moduleDto.setModuleName("Base de données");
        moduleDto.setDescription("Introduction aux bases de données");
        moduleDto.setCredits(3);
        moduleDto.setTotalHours(40);
        moduleDto.setClassId(classDto.getId());

        String requestJson = objectMapper.writeValueAsString(moduleDto);

        mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.moduleCode").value("INF201"))
                .andExpect(jsonPath("$.moduleName").value("Base de données"))
                .andExpect(jsonPath("$.credits").value(3))
                .andExpect(jsonPath("$.totalHours").value(40))
                .andExpect(jsonPath("$.className").value("GI-2A-1"));
    }

    @Test
    @DisplayName("Should fail to create module with duplicate code")
    void testCreateModuleDuplicateCode() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");

        // Create first module
        ModuleDto moduleDto1 = new ModuleDto();
        moduleDto1.setModuleCode("INF201");
        moduleDto1.setModuleName("Base de données");
        moduleDto1.setCredits(3);
        moduleDto1.setTotalHours(40);
        moduleDto1.setClassId(classDto.getId());

        String requestJson1 = objectMapper.writeValueAsString(moduleDto1);

        mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson1))
                .andExpect(status().isCreated());

        // Try to create duplicate
        ModuleDto moduleDto2 = new ModuleDto();
        moduleDto2.setModuleCode("INF201"); // Same code
        moduleDto2.setModuleName("Autre module");
        moduleDto2.setCredits(2);
        moduleDto2.setTotalHours(30);
        moduleDto2.setClassId(classDto.getId());

        String requestJson2 = objectMapper.writeValueAsString(moduleDto2);

        mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Module code already exists"));
    }

    @Test
    @DisplayName("Should fail to create module with invalid class ID")
    void testCreateModuleInvalidClassId() throws Exception {
        ModuleDto moduleDto = new ModuleDto();
        moduleDto.setModuleCode("INF201");
        moduleDto.setModuleName("Base de données");
        moduleDto.setCredits(3);
        moduleDto.setTotalHours(40);
        moduleDto.setClassId(999L); // Non-existent class

        String requestJson = objectMapper.writeValueAsString(moduleDto);

        mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Class not found with id: 999"));
    }

    @Test
    @DisplayName("Teacher should not create module")
    void testCreateModuleAsTeacher() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");

        ModuleDto moduleDto = new ModuleDto();
        moduleDto.setModuleCode("INF201");
        moduleDto.setModuleName("Base de données");
        moduleDto.setCredits(3);
        moduleDto.setTotalHours(40);
        moduleDto.setClassId(classDto.getId());

        String requestJson = objectMapper.writeValueAsString(moduleDto);

        mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin and Teacher should get all modules")
    void testGetAllModules() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        createTestModule("INF201", "Base de données", classDto.getId());
        createTestModule("INF202", "Algorithmique", classDto.getId());

        // Test as Admin
        mockMvc.perform(get("/api/modules")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Test as Teacher
        mockMvc.perform(get("/api/modules")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("All roles should get module by ID")
    void testGetModuleById() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());

        // Test as Admin
        mockMvc.perform(get("/api/modules/" + moduleDto.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moduleCode").value("INF201"));

        // Test as Student
        mockMvc.perform(get("/api/modules/" + moduleDto.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moduleCode").value("INF201"));
    }

    @Test
    @DisplayName("Should get modules by class ID")
    void testGetModulesByClassId() throws Exception {
        AcademicClassDto class1 = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        AcademicClassDto class2 = createTestClass("GI-3A-1", "3A", "Génie Informatique");

        createTestModule("INF201", "Base de données", class1.getId());
        createTestModule("INF202", "Algorithmique", class1.getId());
        createTestModule("INF301", "IA", class2.getId());

        mockMvc.perform(get("/api/modules/class/" + class1.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].classId", everyItem(is(class1.getId().intValue()))));
    }

    @Test
    @DisplayName("Admin should update module successfully")
    void testUpdateModule() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());

        ModuleDto updateDto = new ModuleDto();
        updateDto.setModuleCode("INF201-UPDATED");
        updateDto.setModuleName("Base de données avancées");
        updateDto.setCredits(4);
        updateDto.setTotalHours(50);
        updateDto.setClassId(classDto.getId());

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/modules/" + moduleDto.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moduleCode").value("INF201-UPDATED"))
                .andExpect(jsonPath("$.moduleName").value("Base de données avancées"))
                .andExpect(jsonPath("$.credits").value(4));
    }

    @Test
    @DisplayName("Admin should delete module successfully")
    void testDeleteModule() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());

        mockMvc.perform(delete("/api/modules/" + moduleDto.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/modules/" + moduleDto.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    // Helper methods
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

    private ModuleDto createTestModule(String code, String name, Long classId) throws Exception {
        ModuleDto dto = new ModuleDto();
        dto.setModuleCode(code);
        dto.setModuleName(name);
        dto.setCredits(3);
        dto.setTotalHours(40);
        dto.setClassId(classId);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, ModuleDto.class);
    }
}