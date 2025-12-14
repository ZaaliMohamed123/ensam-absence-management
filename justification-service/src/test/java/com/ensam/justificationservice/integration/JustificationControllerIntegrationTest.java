package com.ensam.justificationservice.integration;

import com.ensam.justificationservice.dto.JustificationDto;
import com.ensam.justificationservice.entities.Justification.JustificationStatus;
import com.ensam.justificationservice.entities.Justification.JustificationType;
import com.ensam.justificationservice.repositories.JustificationRepository;
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

@DisplayName("Justification Controller Integration Tests")
class JustificationControllerIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private JustificationRepository justificationRepository;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @Autowired
    public JustificationControllerIntegrationTest(
            MockMvc mockMvc,
            JustificationRepository justificationRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.justificationRepository = justificationRepository;
    }

    @BeforeEach
    void setUp() {
        justificationRepository.deleteAll();

        adminToken = generateToken("admin@ensam.ma", "ROLE_ADMIN");
        teacherToken = generateToken("teacher@ensam.ma", "ROLE_TEACHER");
        studentToken = generateToken("student@ensam.ma", "ROLE_STUDENT");
    }

    @Test
    @DisplayName("Student should submit justification successfully")
    void testSubmitJustificationAsStudent() throws Exception {
        JustificationDto dto = new JustificationDto();
        dto.setAbsenceId(1L);
        dto.setStudentId("student123");
        dto.setType(JustificationType.MEDICAL);
        dto.setDescription("Certificat médical pour grippe");
        dto.setDocumentUrl("https://example.com/docs/medical-cert-123.pdf");
        dto.setDocumentName("medical-cert-123.pdf");

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/justifications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.absenceId").value(1))
                .andExpect(jsonPath("$.studentId").value("student123"))
                .andExpect(jsonPath("$.type").value("MEDICAL"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.submittedBy").value("student@ensam.ma"));
    }

    @Test
    @DisplayName("Admin should submit justification successfully")
    void testSubmitJustificationAsAdmin() throws Exception {
        JustificationDto dto = new JustificationDto();
        dto.setAbsenceId(2L);
        dto.setStudentId("student456");
        dto.setType(JustificationType.FAMILY);
        dto.setDescription("Raison familiale urgente");

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/justifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.submittedBy").value("admin@ensam.ma"));
    }

    @Test
    @DisplayName("Teacher should not submit justification")
    void testSubmitJustificationAsTeacher() throws Exception {
        JustificationDto dto = new JustificationDto();
        dto.setAbsenceId(3L);
        dto.setStudentId("student789");
        dto.setType(JustificationType.MEDICAL);
        dto.setDescription("Test");

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/justifications")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to submit justification without authentication")
    void testSubmitJustificationWithoutAuth() throws Exception {
        JustificationDto dto = new JustificationDto();
        dto.setAbsenceId(4L);
        dto.setStudentId("student123");
        dto.setType(JustificationType.MEDICAL);
        dto.setDescription("Test");

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/justifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to submit duplicate justification")
    void testSubmitDuplicateJustification() throws Exception {
        // Submit first justification
        JustificationDto dto1 = new JustificationDto();
        dto1.setAbsenceId(5L);
        dto1.setStudentId("student123");
        dto1.setType(JustificationType.MEDICAL);
        dto1.setDescription("First justification");

        String requestJson1 = objectMapper.writeValueAsString(dto1);

        mockMvc.perform(post("/api/justifications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson1))
                .andExpect(status().isCreated());

        // Try to submit duplicate
        JustificationDto dto2 = new JustificationDto();
        dto2.setAbsenceId(5L); // Same absence ID
        dto2.setStudentId("student123");
        dto2.setType(JustificationType.MEDICAL);
        dto2.setDescription("Duplicate justification");

        String requestJson2 = objectMapper.writeValueAsString(dto2);

        mockMvc.perform(post("/api/justifications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Justification already exists for absence ID: 5"));
    }

    @Test
    @DisplayName("Should fail to submit justification with missing fields")
    void testSubmitJustificationMissingFields() throws Exception {
        JustificationDto dto = new JustificationDto();
        dto.setStudentId(""); // Empty student ID

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/justifications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin should get all justifications")
    void testGetAllJustifications() throws Exception {
        createTestJustification(1L, "student123", JustificationType.MEDICAL);
        createTestJustification(2L, "student456", JustificationType.FAMILY);

        mockMvc.perform(get("/api/justifications")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Teacher should get all justifications")
    void testGetAllJustificationsAsTeacher() throws Exception {
        createTestJustification(1L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(get("/api/justifications")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Student should not get all justifications")
    void testGetAllJustificationsAsStudent() throws Exception {
        mockMvc.perform(get("/api/justifications")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("All roles should get justification by ID")
    void testGetJustificationById() throws Exception {
        JustificationDto created = createTestJustification(1L, "student123", JustificationType.MEDICAL);

        // Test as Admin
        mockMvc.perform(get("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("student123"));

        // Test as Teacher
        mockMvc.perform(get("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Test as Student
        mockMvc.perform(get("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 when justification not found")
    void testGetJustificationByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/justifications/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Justification not found with id: 999"));
    }

    @Test
    @DisplayName("Should get justification by absence ID")
    void testGetJustificationByAbsenceId() throws Exception {
        createTestJustification(10L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(get("/api/justifications/absence/10")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.absenceId").value(10))
                .andExpect(jsonPath("$.studentId").value("student123"));
    }

    @Test
    @DisplayName("Should get justifications by student ID")
    void testGetJustificationsByStudentId() throws Exception {
        createTestJustification(11L, "student123", JustificationType.MEDICAL);
        createTestJustification(12L, "student123", JustificationType.FAMILY);
        createTestJustification(13L, "student456", JustificationType.MEDICAL);

        mockMvc.perform(get("/api/justifications/student/student123")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].studentId", everyItem(is("student123"))));
    }

    @Test
    @DisplayName("Should get justifications by status")
    void testGetJustificationsByStatus() throws Exception {
        JustificationDto j1 = createTestJustification(14L, "student123", JustificationType.MEDICAL);
        JustificationDto j2 = createTestJustification(15L, "student456", JustificationType.FAMILY);
        JustificationDto j3 = createTestJustification(16L, "student789", JustificationType.MEDICAL);

        // Approve one justification
        mockMvc.perform(patch("/api/justifications/" + j3.getId() + "/approve")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/justifications/status/PENDING")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("Should get justifications by type")
    void testGetJustificationsByType() throws Exception {
        createTestJustification(17L, "student123", JustificationType.MEDICAL);
        createTestJustification(18L, "student456", JustificationType.MEDICAL);
        createTestJustification(19L, "student789", JustificationType.FAMILY);

        mockMvc.perform(get("/api/justifications/type/MEDICAL")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].type", everyItem(is("MEDICAL"))));
    }

    @Test
    @DisplayName("Should get pending count for student")
    void testGetPendingCount() throws Exception {
        createTestJustification(20L, "student123", JustificationType.MEDICAL);
        createTestJustification(21L, "student123", JustificationType.FAMILY);
        JustificationDto j3 = createTestJustification(22L, "student123", JustificationType.MEDICAL);

        // Approve one
        mockMvc.perform(patch("/api/justifications/" + j3.getId() + "/approve")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/justifications/student/student123/pending/count")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(2));
    }

    @Test
    @DisplayName("Teacher should approve justification")
    void testApproveJustification() throws Exception {
        JustificationDto created = createTestJustification(23L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(patch("/api/justifications/" + created.getId() + "/approve")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewedBy").value("teacher@ensam.ma"))
                .andExpect(jsonPath("$.reviewedAt").exists());
    }

    @Test
    @DisplayName("Admin should approve justification")
    void testApproveJustificationAsAdmin() throws Exception {
        JustificationDto created = createTestJustification(24L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(patch("/api/justifications/" + created.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Student should not approve justification")
    void testApproveJustificationAsStudent() throws Exception {
        JustificationDto created = createTestJustification(25L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(patch("/api/justifications/" + created.getId() + "/approve")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to approve already reviewed justification")
    void testApproveAlreadyReviewedJustification() throws Exception {
        JustificationDto created = createTestJustification(26L, "student123", JustificationType.MEDICAL);

        // First approval
        mockMvc.perform(patch("/api/justifications/" + created.getId() + "/approve")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Try to approve again
        mockMvc.perform(patch("/api/justifications/" + created.getId() + "/approve")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Justification has already been reviewed"));
    }

    @Test
    @DisplayName("Teacher should reject justification")
    void testRejectJustification() throws Exception {
        JustificationDto created = createTestJustification(27L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(patch("/api/justifications/" + created.getId() + "/reject")
                        .param("reason", "Document illisible")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Document illisible"))
                .andExpect(jsonPath("$.reviewedBy").value("teacher@ensam.ma"))
                .andExpect(jsonPath("$.reviewedAt").exists());
    }

    @Test
    @DisplayName("Student should not reject justification")
    void testRejectJustificationAsStudent() throws Exception {
        JustificationDto created = createTestJustification(28L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(patch("/api/justifications/" + created.getId() + "/reject")
                        .param("reason", "Test")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student should update pending justification")
    void testUpdateJustification() throws Exception {
        JustificationDto created = createTestJustification(29L, "student123", JustificationType.MEDICAL);

        JustificationDto updateDto = new JustificationDto();
        updateDto.setAbsenceId(29L);
        updateDto.setStudentId("student123");
        updateDto.setType(JustificationType.FAMILY);
        updateDto.setDescription("Updated description - raison familiale");
        updateDto.setDocumentUrl("https://example.com/docs/updated.pdf");
        updateDto.setDocumentName("updated.pdf");

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FAMILY"))
                .andExpect(jsonPath("$.description").value("Updated description - raison familiale"));
    }

    @Test
    @DisplayName("Should fail to update approved justification")
    void testUpdateApprovedJustification() throws Exception {
        JustificationDto created = createTestJustification(30L, "student123", JustificationType.MEDICAL);

        // Approve it first
        mockMvc.perform(patch("/api/justifications/" + created.getId() + "/approve")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Try to update
        JustificationDto updateDto = new JustificationDto();
        updateDto.setAbsenceId(30L);
        updateDto.setStudentId("student123");
        updateDto.setType(JustificationType.FAMILY);
        updateDto.setDescription("Should not work");

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot update justification that has been reviewed"));
    }

    @Test
    @DisplayName("Teacher should not update justification")
    void testUpdateJustificationAsTeacher() throws Exception {
        JustificationDto created = createTestJustification(31L, "student123", JustificationType.MEDICAL);

        JustificationDto updateDto = new JustificationDto();
        updateDto.setAbsenceId(31L);
        updateDto.setStudentId("student123");
        updateDto.setType(JustificationType.FAMILY);
        updateDto.setDescription("Test");

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should delete justification")
    void testDeleteJustification() throws Exception {
        JustificationDto created = createTestJustification(32L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(delete("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Teacher should not delete justification")
    void testDeleteJustificationAsTeacher() throws Exception {
        JustificationDto created = createTestJustification(33L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(delete("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student should not delete justification")
    void testDeleteJustificationAsStudent() throws Exception {
        JustificationDto created = createTestJustification(34L, "student123", JustificationType.MEDICAL);

        mockMvc.perform(delete("/api/justifications/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    // Helper method
    private JustificationDto createTestJustification(Long absenceId, String studentId, JustificationType type) throws Exception {
        JustificationDto dto = new JustificationDto();
        dto.setAbsenceId(absenceId);
        dto.setStudentId(studentId);
        dto.setType(type);
        dto.setDescription("Test justification for " + type);
        dto.setDocumentUrl("https://example.com/docs/test-" + absenceId + ".pdf");
        dto.setDocumentName("test-" + absenceId + ".pdf");

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/justifications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, JustificationDto.class);
    }
}
