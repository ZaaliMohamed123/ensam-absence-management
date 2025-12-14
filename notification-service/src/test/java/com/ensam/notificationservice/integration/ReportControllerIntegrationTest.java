package com.ensam.notificationservice.integration;

import com.ensam.notificationservice.dto.ReportDto;
import com.ensam.notificationservice.entities.Report.ReportPeriod;
import com.ensam.notificationservice.entities.Report.ReportType;
import com.ensam.notificationservice.repositories.ReportRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Report Controller Integration Tests")
class ReportControllerIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ReportRepository reportRepository;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @Autowired
    public ReportControllerIntegrationTest(
            MockMvc mockMvc,
            ReportRepository reportRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.reportRepository = reportRepository;
    }

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();

        adminToken = generateToken("admin@ensam.ma", "ROLE_ADMIN");
        teacherToken = generateToken("teacher@ensam.ma", "ROLE_TEACHER");
        studentToken = generateToken("student@ensam.ma", "ROLE_STUDENT");
    }

    @Test
    @DisplayName("Teacher should generate report successfully")
    void testGenerateReportAsTeacher() throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReportType(ReportType.STUDENT);
        dto.setTargetId("student123");
        dto.setTargetName("John Doe");
        dto.setPeriod(ReportPeriod.WEEKLY);
        dto.setPeriodStart(LocalDate.now().minusDays(7));
        dto.setPeriodEnd(LocalDate.now());
        dto.setReportData("{\"absences\": []}");
        dto.setTotalAbsences(5L);
        dto.setJustifiedAbsences(2L);
        dto.setUnjustifiedAbsences(3L);
        dto.setAbsenceRate(60.0);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.reportType").value("STUDENT"))
                .andExpect(jsonPath("$.targetId").value("student123"))
                .andExpect(jsonPath("$.period").value("WEEKLY"))
                .andExpect(jsonPath("$.totalAbsences").value(5))
                .andExpect(jsonPath("$.generatedBy").value("teacher@ensam.ma"));
    }

    @Test
    @DisplayName("Admin should generate report successfully")
    void testGenerateReportAsAdmin() throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReportType(ReportType.CLASS);
        dto.setTargetId("class-gi-2a-1");
        dto.setTargetName("GI-2A-1");
        dto.setPeriod(ReportPeriod.MONTHLY);
        dto.setPeriodStart(LocalDate.now().minusDays(30));
        dto.setPeriodEnd(LocalDate.now());
        dto.setReportData("{\"summary\": \"Monthly report\"}");
        dto.setTotalAbsences(50L);
        dto.setJustifiedAbsences(20L);
        dto.setUnjustifiedAbsences(30L);
        dto.setAbsenceRate(40.0);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generatedBy").value("admin@ensam.ma"));
    }

    @Test
    @DisplayName("Student should not generate report")
    void testGenerateReportAsStudent() throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReportType(ReportType.STUDENT);
        dto.setTargetId("student123");
        dto.setPeriod(ReportPeriod.WEEKLY);
        dto.setPeriodStart(LocalDate.now().minusDays(7));
        dto.setPeriodEnd(LocalDate.now());
        dto.setReportData("{}");

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to generate report without authentication")
    void testGenerateReportWithoutAuth() throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReportType(ReportType.STUDENT);
        dto.setTargetId("student123");
        dto.setPeriod(ReportPeriod.WEEKLY);
        dto.setPeriodStart(LocalDate.now().minusDays(7));
        dto.setPeriodEnd(LocalDate.now());
        dto.setReportData("{}");

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to generate report with missing fields")
    void testGenerateReportMissingFields() throws Exception {
        ReportDto dto = new ReportDto();
        dto.setTargetId(""); // Empty target ID

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin should get all reports")
    void testGetAllReports() throws Exception {
        createTestReport("student123", ReportType.STUDENT);
        createTestReport("class-gi-2a-1", ReportType.CLASS);

        mockMvc.perform(get("/api/reports")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Teacher should not get all reports")
    void testGetAllReportsAsTeacher() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("All roles should get report by ID")
    void testGetReportById() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        // Test as Admin
        mockMvc.perform(get("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetId").value("student123"));

        // Test as Teacher
        mockMvc.perform(get("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Test as Student
        mockMvc.perform(get("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 when report not found")
    void testGetReportByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/reports/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Report not found with id: 999"));
    }

    @Test
    @DisplayName("Should get reports by target ID")
    void testGetReportsByTargetId() throws Exception {
        createTestReport("student123", ReportType.STUDENT);
        createTestReport("student123", ReportType.STUDENT);
        createTestReport("student456", ReportType.STUDENT);

        mockMvc.perform(get("/api/reports/target/student123")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].targetId", everyItem(is("student123"))));
    }

    @Test
    @DisplayName("Should get reports by type")
    void testGetReportsByType() throws Exception {
        createTestReport("student123", ReportType.STUDENT);
        createTestReport("student456", ReportType.STUDENT);
        createTestReport("class-gi-2a-1", ReportType.CLASS);

        mockMvc.perform(get("/api/reports/type/STUDENT")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].reportType", everyItem(is("STUDENT"))));
    }

    @Test
    @DisplayName("Should get reports by period")
    void testGetReportsByPeriod() throws Exception {
        createTestReportWithPeriod("student123", ReportPeriod.WEEKLY);
        createTestReportWithPeriod("student456", ReportPeriod.WEEKLY);
        createTestReportWithPeriod("student789", ReportPeriod.MONTHLY);

        mockMvc.perform(get("/api/reports/period/WEEKLY")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].period", everyItem(is("WEEKLY"))));
    }

    @Test
    @DisplayName("Should get recent reports")
    void testGetRecentReports() throws Exception {
        createTestReport("student123", ReportType.STUDENT);
        createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(get("/api/reports/target/student123/recent")
                        .param("daysBack", "30")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Admin should delete report")
    void testDeleteReport() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(delete("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Teacher should not delete report")
    void testDeleteReportAsTeacher() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(delete("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    // Helper methods
    private ReportDto createTestReport(String targetId, ReportType reportType) throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReportType(reportType);
        dto.setTargetId(targetId);
        dto.setTargetName("Target " + targetId);
        dto.setPeriod(ReportPeriod.WEEKLY);
        dto.setPeriodStart(LocalDate.now().minusDays(7));
        dto.setPeriodEnd(LocalDate.now());
        dto.setReportData("{\"test\": \"data\"}");
        dto.setTotalAbsences(10L);
        dto.setJustifiedAbsences(4L);
        dto.setUnjustifiedAbsences(6L);
        dto.setAbsenceRate(60.0);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, ReportDto.class);
    }

    private ReportDto createTestReportWithPeriod(String targetId, ReportPeriod period) throws Exception {
        ReportDto dto = new ReportDto();
        dto.setReportType(ReportType.STUDENT);
        dto.setTargetId(targetId);
        dto.setTargetName("Target " + targetId);
        dto.setPeriod(period);
        dto.setPeriodStart(LocalDate.now().minusDays(7));
        dto.setPeriodEnd(LocalDate.now());
        dto.setReportData("{}");
        dto.setTotalAbsences(5L);
        dto.setJustifiedAbsences(2L);
        dto.setUnjustifiedAbsences(3L);
        dto.setAbsenceRate(60.0);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, ReportDto.class);
    }
}
