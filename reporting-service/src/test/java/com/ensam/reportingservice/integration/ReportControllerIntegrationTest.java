package com.ensam.reportingservice.integration;

import com.ensam.reportingservice.dto.ReportDto;
import com.ensam.reportingservice.entities.Report.ReportPeriod;
import com.ensam.reportingservice.entities.Report.ReportStatus;
import com.ensam.reportingservice.entities.Report.ReportType;
import com.ensam.reportingservice.repositories.ReportRepository;
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
        ReportDto dto = createReportDto("student123", ReportType.STUDENT, ReportPeriod.WEEKLY);

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
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.generatedBy").value("teacher@ensam.ma"));
    }

    @Test
    @DisplayName("Admin should generate report successfully")
    void testGenerateReportAsAdmin() throws Exception {
        ReportDto dto = createReportDto("class-gi-2a-1", ReportType.CLASS, ReportPeriod.MONTHLY);

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
        ReportDto dto = createReportDto("student123", ReportType.STUDENT, ReportPeriod.WEEKLY);

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
        ReportDto dto = createReportDto("student123", ReportType.STUDENT, ReportPeriod.WEEKLY);

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
    @DisplayName("Should generate report with alert for high absence rate")
    void testGenerateReportWithAlert() throws Exception {
        ReportDto dto = createReportDto("student123", ReportType.STUDENT, ReportPeriod.WEEKLY);
        dto.setAbsenceRate(25.0); // > 20% threshold
        dto.setUnjustifiedAbsences(5L); // > 3 threshold

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasAlert").value(true))
                .andExpect(jsonPath("$.alertMessage").exists());
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
    @DisplayName("Should get reports by status")
    void testGetReportsByStatus() throws Exception {
        ReportDto r1 = createTestReport("student123", ReportType.STUDENT);
        ReportDto r2 = createTestReport("student456", ReportType.STUDENT);
        ReportDto r3 = createTestReport("student789", ReportType.STUDENT);

        // Publish one report
        mockMvc.perform(patch("/api/reports/" + r3.getId() + "/publish")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reports/status/DRAFT")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", everyItem(is("DRAFT"))));
    }

    @Test
    @DisplayName("Should get reports with alerts")
    void testGetReportsWithAlerts() throws Exception {
        // Report without alert
        createTestReport("student123", ReportType.STUDENT);

        // Report with alert (high absence rate)
        ReportDto dto = createReportDto("student456", ReportType.STUDENT, ReportPeriod.WEEKLY);
        dto.setAbsenceRate(25.0);
        String requestJson = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/alerts")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].hasAlert").value(true));
    }

    @Test
    @DisplayName("Should get recent reports")
    void testGetRecentReports() throws Exception {
        createTestReport("student123", ReportType.STUDENT);
        createTestReport("student456", ReportType.STUDENT);

        mockMvc.perform(get("/api/reports/recent")
                        .param("days", "30")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should get reports by date range")
    void testGetReportsByDateRange() throws Exception {
        createTestReport("student123", ReportType.STUDENT);
        createTestReport("student456", ReportType.STUDENT);

        String startDate = LocalDate.now().minusDays(7).toString();
        String endDate = LocalDate.now().toString();

        mockMvc.perform(get("/api/reports/daterange")
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should get latest report")
    void testGetLatestReport() throws Exception {
        createTestReport("student123", ReportType.STUDENT);
        Thread.sleep(100); // Ensure different timestamps
        ReportDto latest = createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(get("/api/reports/latest")
                        .param("targetId", "student123")
                        .param("reportType", "STUDENT")
                        .param("period", "WEEKLY")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(latest.getId()));
    }

    @Test
    @DisplayName("Should get reports summary")
    void testGetReportsSummary() throws Exception {
        ReportDto r1 = createTestReport("student123", ReportType.STUDENT);
        ReportDto r2 = createTestReport("student456", ReportType.STUDENT);

        // Publish one
        mockMvc.perform(patch("/api/reports/" + r1.getId() + "/publish")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reports/summary")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReports").value(2))
                .andExpect(jsonPath("$.publishedReports").value(1))
                .andExpect(jsonPath("$.draftReports").value(1));
    }

    @Test
    @DisplayName("Teacher should publish report")
    void testPublishReport() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(patch("/api/reports/" + created.getId() + "/publish")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedBy").value("teacher@ensam.ma"))
                .andExpect(jsonPath("$.publishedAt").exists());
    }

    @Test
    @DisplayName("Admin should publish report")
    void testPublishReportAsAdmin() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(patch("/api/reports/" + created.getId() + "/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("Student should not publish report")
    void testPublishReportAsStudent() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(patch("/api/reports/" + created.getId() + "/publish")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should archive report")
    void testArchiveReport() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(patch("/api/reports/" + created.getId() + "/archive")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    @DisplayName("Teacher should not archive report")
    void testArchiveReportAsTeacher() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(patch("/api/reports/" + created.getId() + "/archive")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should update draft report")
    void testUpdateReport() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        ReportDto updateDto = createReportDto("student123", ReportType.STUDENT, ReportPeriod.WEEKLY);
        updateDto.setTotalAbsences(10L);
        updateDto.setJustifiedAbsences(4L);
        updateDto.setUnjustifiedAbsences(6L);
        updateDto.setAbsenceRate(60.0);

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAbsences").value(10))
                .andExpect(jsonPath("$.absenceRate").value(60.0));
    }

    @Test
    @DisplayName("Should fail to update published report")
    void testUpdatePublishedReport() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        // Publish it first
        mockMvc.perform(patch("/api/reports/" + created.getId() + "/publish")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Try to update
        ReportDto updateDto = createReportDto("student123", ReportType.STUDENT, ReportPeriod.WEEKLY);
        updateDto.setTotalAbsences(20L);

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only draft reports can be updated"));
    }

    @Test
    @DisplayName("Student should not update report")
    void testUpdateReportAsStudent() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        ReportDto updateDto = createReportDto("student123", ReportType.STUDENT, ReportPeriod.WEEKLY);
        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
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

    @Test
    @DisplayName("Student should not delete report")
    void testDeleteReportAsStudent() throws Exception {
        ReportDto created = createTestReport("student123", ReportType.STUDENT);

        mockMvc.perform(delete("/api/reports/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    // Helper methods
    private ReportDto createReportDto(String targetId, ReportType reportType, ReportPeriod period) {
        ReportDto dto = new ReportDto();
        dto.setReportType(reportType);
        dto.setTargetId(targetId);
        dto.setTargetName("Target " + targetId);
        dto.setPeriod(period);
        dto.setPeriodStart(LocalDate.now().minusDays(7));
        dto.setPeriodEnd(LocalDate.now());
        dto.setReportData("{\"data\": \"test\"}");
        dto.setTotalAbsences(5L);
        dto.setJustifiedAbsences(2L);
        dto.setUnjustifiedAbsences(3L);
        dto.setPendingJustifications(0L);
        dto.setAbsenceRate(15.0);
        return dto;
    }

    private ReportDto createTestReport(String targetId, ReportType reportType) throws Exception {
        ReportDto dto = createReportDto(targetId, reportType, ReportPeriod.WEEKLY);

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
        ReportDto dto = createReportDto(targetId, ReportType.STUDENT, period);

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
