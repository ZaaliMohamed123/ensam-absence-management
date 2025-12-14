package com.ensam.absenceservice.integration;

import com.ensam.absenceservice.dto.AbsenceDto;
import com.ensam.absenceservice.entities.Absence.AbsenceStatus;
import com.ensam.absenceservice.repositories.AbsenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Absence Controller Integration Tests")
class AbsenceControllerIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AbsenceRepository absenceRepository;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @Autowired
    public AbsenceControllerIntegrationTest(
            MockMvc mockMvc,
            AbsenceRepository absenceRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.absenceRepository = absenceRepository;
    }

    @BeforeEach
    void setUp() {
        absenceRepository.deleteAll();

        adminToken = generateToken("admin@ensam.ma", "ROLE_ADMIN");
        teacherToken = generateToken("teacher@ensam.ma", "ROLE_TEACHER");
        studentToken = generateToken("student@ensam.ma", "ROLE_STUDENT");
    }

    @Test
    @DisplayName("Teacher should create absence successfully")
    void testCreateAbsenceAsTeacher() throws Exception {
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId("student123");
        dto.setTeacherId("teacher456");
        dto.setScheduleId(1L);
        dto.setModuleId(1L);
        dto.setClassId(1L);
        dto.setAbsenceDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(AbsenceStatus.PENDING);
        dto.setNotes("Absent sans justification");

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.studentId").value("student123"))
                .andExpect(jsonPath("$.teacherId").value("teacher456"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.notified").value(false));
    }

    @Test
    @DisplayName("Admin should create absence successfully")
    void testCreateAbsenceAsAdmin() throws Exception {
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId("student123");
        dto.setTeacherId("teacher456");
        dto.setScheduleId(1L);
        dto.setModuleId(1L);
        dto.setClassId(1L);
        dto.setAbsenceDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(AbsenceStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Student should not create absence")
    void testCreateAbsenceAsStudent() throws Exception {
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId("student123");
        dto.setTeacherId("teacher456");
        dto.setScheduleId(1L);
        dto.setModuleId(1L);
        dto.setClassId(1L);
        dto.setAbsenceDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(AbsenceStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to create absence without authentication")
    void testCreateAbsenceWithoutAuth() throws Exception {
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId("student123");
        dto.setTeacherId("teacher456");
        dto.setScheduleId(1L);
        dto.setModuleId(1L);
        dto.setClassId(1L);
        dto.setAbsenceDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(AbsenceStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/absences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to create duplicate absence")
    void testCreateDuplicateAbsence() throws Exception {
        // Create first absence
        AbsenceDto dto1 = new AbsenceDto();
        dto1.setStudentId("student123");
        dto1.setTeacherId("teacher456");
        dto1.setScheduleId(1L);
        dto1.setModuleId(1L);
        dto1.setClassId(1L);
        dto1.setAbsenceDate(LocalDate.now());
        dto1.setStartTime(LocalTime.of(8, 0));
        dto1.setEndTime(LocalTime.of(10, 0));
        dto1.setStatus(AbsenceStatus.PENDING);

        String requestJson1 = objectMapper.writeValueAsString(dto1);

        mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson1))
                .andExpect(status().isCreated());

        // Try to create duplicate
        AbsenceDto dto2 = new AbsenceDto();
        dto2.setStudentId("student123");
        dto2.setTeacherId("teacher456");
        dto2.setScheduleId(1L);
        dto2.setModuleId(1L);
        dto2.setClassId(1L);
        dto2.setAbsenceDate(LocalDate.now());
        dto2.setStartTime(LocalTime.of(8, 0));
        dto2.setEndTime(LocalTime.of(10, 0));
        dto2.setStatus(AbsenceStatus.PENDING);

        String requestJson2 = objectMapper.writeValueAsString(dto2);

        mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Absence already exists for this student, schedule and date"));
    }

    @Test
    @DisplayName("Should fail to create absence with missing required fields")
    void testCreateAbsenceMissingFields() throws Exception {
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId(""); // Empty student ID

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin should get all absences")
    void testGetAllAbsences() throws Exception {
        // Create test absences
        createTestAbsence("student123", "teacher456", LocalDate.now());
        createTestAbsence("student456", "teacher456", LocalDate.now());

        mockMvc.perform(get("/api/absences")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Teacher should not get all absences")
    void testGetAllAbsencesAsTeacher() throws Exception {
        mockMvc.perform(get("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("All roles should get absence by ID")
    void testGetAbsenceById() throws Exception {
        AbsenceDto created = createTestAbsence("student123", "teacher456", LocalDate.now());

        // Test as Admin
        mockMvc.perform(get("/api/absences/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("student123"));

        // Test as Teacher
        mockMvc.perform(get("/api/absences/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("student123"));

        // Test as Student
        mockMvc.perform(get("/api/absences/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("student123"));
    }

    @Test
    @DisplayName("Should return 400 when absence not found")
    void testGetAbsenceByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/absences/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Absence not found with id: 999"));
    }

    @Test
    @DisplayName("Should get absences by student ID")
    void testGetAbsencesByStudentId() throws Exception {
        createTestAbsence("student123", "teacher456", LocalDate.now());
        createTestAbsence("student123", "teacher456", LocalDate.now().minusDays(1));
        createTestAbsence("student456", "teacher456", LocalDate.now());

        mockMvc.perform(get("/api/absences/student/student123")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].studentId", everyItem(is("student123"))));
    }

    @Test
    @DisplayName("Should get absences by teacher ID")
    void testGetAbsencesByTeacherId() throws Exception {
        createTestAbsence("student123", "teacher456", LocalDate.now());
        createTestAbsence("student456", "teacher456", LocalDate.now());
        createTestAbsence("student789", "teacher789", LocalDate.now());

        mockMvc.perform(get("/api/absences/teacher/teacher456")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].teacherId", everyItem(is("teacher456"))));
    }

    @Test
    @DisplayName("Student should not get absences by teacher ID")
    void testGetAbsencesByTeacherIdAsStudent() throws Exception {
        mockMvc.perform(get("/api/absences/teacher/teacher456")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get absences by class ID")
    void testGetAbsencesByClassId() throws Exception {
        createTestAbsenceWithClass("student123", 1L);
        createTestAbsenceWithClass("student456", 1L);
        createTestAbsenceWithClass("student789", 2L);

        mockMvc.perform(get("/api/absences/class/1")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].classId", everyItem(is(1))));
    }

    @Test
    @DisplayName("Should get absences by module ID")
    void testGetAbsencesByModuleId() throws Exception {
        createTestAbsenceWithModule("student123", 1L);
        createTestAbsenceWithModule("student456", 1L);
        createTestAbsenceWithModule("student789", 2L);

        mockMvc.perform(get("/api/absences/module/1")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].moduleId", everyItem(is(1))));
    }

    @Test
    @DisplayName("Should get absences by status")
    void testGetAbsencesByStatus() throws Exception {
        createTestAbsenceWithStatus("student123", AbsenceStatus.PENDING);
        createTestAbsenceWithStatus("student456", AbsenceStatus.JUSTIFIED);
        createTestAbsenceWithStatus("student789", AbsenceStatus.PENDING);

        mockMvc.perform(get("/api/absences/status/PENDING")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("Should get absences by student and date range")
    void testGetAbsencesByStudentAndDateRange() throws Exception {
        createTestAbsence("student123", "teacher456", LocalDate.now().minusDays(5));
        createTestAbsence("student123", "teacher456", LocalDate.now().minusDays(3));
        createTestAbsence("student123", "teacher456", LocalDate.now().minusDays(1));
        createTestAbsence("student123", "teacher456", LocalDate.now().minusDays(10));

        String startDate = LocalDate.now().minusDays(7).toString();
        String endDate = LocalDate.now().toString();

        mockMvc.perform(get("/api/absences/student/student123/daterange")
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Teacher should update absence status")
    void testUpdateAbsenceStatus() throws Exception {
        AbsenceDto created = createTestAbsence("student123", "teacher456", LocalDate.now());

        mockMvc.perform(patch("/api/absences/" + created.getId() + "/status")
                        .param("status", "JUSTIFIED")
                        .param("notes", "Justificatif médical fourni")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("JUSTIFIED"))
                .andExpect(jsonPath("$.notes").value("Justificatif médical fourni"));
    }

    @Test
    @DisplayName("Student should not update absence status")
    void testUpdateAbsenceStatusAsStudent() throws Exception {
        AbsenceDto created = createTestAbsence("student123", "teacher456", LocalDate.now());

        mockMvc.perform(patch("/api/absences/" + created.getId() + "/status")
                        .param("status", "JUSTIFIED")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Teacher should update absence")
    void testUpdateAbsence() throws Exception {
        AbsenceDto created = createTestAbsence("student123", "teacher456", LocalDate.now());

        AbsenceDto updateDto = new AbsenceDto();
        updateDto.setStudentId("student123");
        updateDto.setTeacherId("teacher456");
        updateDto.setScheduleId(1L);
        updateDto.setModuleId(1L);
        updateDto.setClassId(1L);
        updateDto.setAbsenceDate(LocalDate.now().plusDays(1));
        updateDto.setStartTime(LocalTime.of(10, 0));
        updateDto.setEndTime(LocalTime.of(12, 0));
        updateDto.setStatus(AbsenceStatus.UNJUSTIFIED);
        updateDto.setNotes("Updated notes");
        updateDto.setNotified(true);

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/absences/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNJUSTIFIED"))
                .andExpect(jsonPath("$.notes").value("Updated notes"))
                .andExpect(jsonPath("$.notified").value(true));
    }

    @Test
    @DisplayName("Admin should delete absence")
    void testDeleteAbsence() throws Exception {
        AbsenceDto created = createTestAbsence("student123", "teacher456", LocalDate.now());

        mockMvc.perform(delete("/api/absences/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/absences/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Teacher should not delete absence")
    void testDeleteAbsenceAsTeacher() throws Exception {
        AbsenceDto created = createTestAbsence("student123", "teacher456", LocalDate.now());

        mockMvc.perform(delete("/api/absences/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get student absence statistics")
    void testGetStudentAbsenceStats() throws Exception {
        createTestAbsenceWithStatus("student123", AbsenceStatus.PENDING);
        createTestAbsenceWithStatus("student123", AbsenceStatus.JUSTIFIED);
        createTestAbsenceWithStatus("student123", AbsenceStatus.UNJUSTIFIED);
        createTestAbsenceWithStatus("student123", AbsenceStatus.UNJUSTIFIED);

        mockMvc.perform(get("/api/absences/stats/student/student123")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAbsences").value(4))
                .andExpect(jsonPath("$.justifiedAbsences").value(1))
                .andExpect(jsonPath("$.unjustifiedAbsences").value(2))
                .andExpect(jsonPath("$.pendingAbsences").value(1))
                .andExpect(jsonPath("$.absenceRate").value(50.0)); // 2/4 * 100
    }

    @Test
    @DisplayName("Should get student module absence statistics")
    void testGetStudentModuleAbsenceStats() throws Exception {
        createTestAbsenceWithModuleAndStatus("student123", 1L, AbsenceStatus.UNJUSTIFIED);
        createTestAbsenceWithModuleAndStatus("student123", 1L, AbsenceStatus.UNJUSTIFIED);
        createTestAbsenceWithModuleAndStatus("student123", 1L, AbsenceStatus.JUSTIFIED);
        createTestAbsenceWithModuleAndStatus("student123", 2L, AbsenceStatus.UNJUSTIFIED);

        mockMvc.perform(get("/api/absences/stats/student/student123/modules")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].moduleId").value(1))
                .andExpect(jsonPath("$[0].totalAbsences").value(3))
                .andExpect(jsonPath("$[0].unjustifiedAbsences").value(2))
                .andExpect(jsonPath("$[0].isAtRisk").value(false)); // 2 < 3 threshold
    }

    // Helper methods
    private AbsenceDto createTestAbsence(String studentId, String teacherId, LocalDate date) throws Exception {
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId(studentId);
        dto.setTeacherId(teacherId);
        dto.setScheduleId(System.currentTimeMillis()); // Unique schedule ID
        dto.setModuleId(1L);
        dto.setClassId(1L);
        dto.setAbsenceDate(date);
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(AbsenceStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, AbsenceDto.class);
    }

    private AbsenceDto createTestAbsenceWithClass(String studentId, Long classId) throws Exception {
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId(studentId);
        dto.setTeacherId("teacher456");
        dto.setScheduleId(System.currentTimeMillis());
        dto.setModuleId(1L);
        dto.setClassId(classId);
        dto.setAbsenceDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(AbsenceStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, AbsenceDto.class);
    }

    private AbsenceDto createTestAbsenceWithModule(String studentId, Long moduleId) throws Exception {
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId(studentId);
        dto.setTeacherId("teacher456");
        dto.setScheduleId(System.currentTimeMillis());
        dto.setModuleId(moduleId);
        dto.setClassId(1L);
        dto.setAbsenceDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(AbsenceStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, AbsenceDto.class);
    }

    private AbsenceDto createTestAbsenceWithStatus(String studentId, AbsenceStatus status) throws Exception {
        // Create absence (will be PENDING by default)
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId(studentId);
        dto.setTeacherId("teacher456");
        dto.setScheduleId(System.currentTimeMillis());
        dto.setModuleId(1L);
        dto.setClassId(1L);
        dto.setAbsenceDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(AbsenceStatus.PENDING); // Le service va ignorer ceci

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AbsenceDto created = objectMapper.readValue(response, AbsenceDto.class);

        // ✅ Update status if not PENDING
        if (status != AbsenceStatus.PENDING) {
            String updateResponse = mockMvc.perform(patch("/api/absences/" + created.getId() + "/status")
                            .param("status", status.toString())
                            .header("Authorization", "Bearer " + teacherToken))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            return objectMapper.readValue(updateResponse, AbsenceDto.class);
        }

        return created;
    }


    private AbsenceDto createTestAbsenceWithModuleAndStatus(String studentId, Long moduleId, AbsenceStatus status) throws Exception {
        // Create absence (will be PENDING by default)
        AbsenceDto dto = new AbsenceDto();
        dto.setStudentId(studentId);
        dto.setTeacherId("teacher456");
        dto.setScheduleId(System.currentTimeMillis());
        dto.setModuleId(moduleId);
        dto.setClassId(1L);
        dto.setAbsenceDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(AbsenceStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/absences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AbsenceDto created = objectMapper.readValue(response, AbsenceDto.class);

        // ✅ Update status if not PENDING
        if (status != AbsenceStatus.PENDING) {
            String updateResponse = mockMvc.perform(patch("/api/absences/" + created.getId() + "/status")
                            .param("status", status.toString())
                            .header("Authorization", "Bearer " + teacherToken))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            return objectMapper.readValue(updateResponse, AbsenceDto.class);
        }

        return created;
    }

}
