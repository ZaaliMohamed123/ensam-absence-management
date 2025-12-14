package com.ensam.notificationservice.integration;

import com.ensam.notificationservice.dto.NotificationDto;
import com.ensam.notificationservice.entities.Notification.NotificationStatus;
import com.ensam.notificationservice.entities.Notification.NotificationType;
import com.ensam.notificationservice.entities.Notification.RecipientType;
import com.ensam.notificationservice.repositories.NotificationRepository;
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

@DisplayName("Notification Controller Integration Tests")
class NotificationControllerIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private NotificationRepository notificationRepository;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @Autowired
    public NotificationControllerIntegrationTest(
            MockMvc mockMvc,
            NotificationRepository notificationRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.notificationRepository = notificationRepository;
    }

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        adminToken = generateToken("admin@ensam.ma", "ROLE_ADMIN");
        teacherToken = generateToken("teacher@ensam.ma", "ROLE_TEACHER");
        studentToken = generateToken("student@ensam.ma", "ROLE_STUDENT");
    }

    @Test
    @DisplayName("Teacher should create notification successfully")
    void testCreateNotificationAsTeacher() throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setRecipientId("student123");
        dto.setRecipientType(RecipientType.STUDENT);
        dto.setSubject("Absence Alert");
        dto.setMessage("You have been marked absent for today's class");
        dto.setType(NotificationType.ABSENCE_ALERT);
        dto.setRelatedAbsenceId(1L);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.recipientId").value("student123"))
                .andExpect(jsonPath("$.subject").value("Absence Alert"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.isRead").value(false));
    }

    @Test
    @DisplayName("Admin should create notification successfully")
    void testCreateNotificationAsAdmin() throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setRecipientId("teacher456");
        dto.setRecipientType(RecipientType.TEACHER);
        dto.setSubject("System Message");
        dto.setMessage("Please submit your weekly reports");
        dto.setType(NotificationType.SYSTEM_MESSAGE);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Student should not create notification")
    void testCreateNotificationAsStudent() throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setRecipientId("student123");
        dto.setRecipientType(RecipientType.STUDENT);
        dto.setSubject("Test");
        dto.setMessage("Test message");
        dto.setType(NotificationType.SYSTEM_MESSAGE);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to create notification without authentication")
    void testCreateNotificationWithoutAuth() throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setRecipientId("student123");
        dto.setRecipientType(RecipientType.STUDENT);
        dto.setSubject("Test");
        dto.setMessage("Test message");
        dto.setType(NotificationType.SYSTEM_MESSAGE);

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to create notification with missing fields")
    void testCreateNotificationMissingFields() throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setRecipientId(""); // Empty recipient ID

        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin should get all notifications")
    void testGetAllNotifications() throws Exception {
        createTestNotification("student123", "Absence Alert 1");
        createTestNotification("student456", "Absence Alert 2");

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Teacher should not get all notifications")
    void testGetAllNotificationsAsTeacher() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("All roles should get notification by ID")
    void testGetNotificationById() throws Exception {
        NotificationDto created = createTestNotification("student123", "Test Subject");

        // Test as Admin
        mockMvc.perform(get("/api/notifications/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Test Subject"));

        // Test as Teacher
        mockMvc.perform(get("/api/notifications/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Test as Student
        mockMvc.perform(get("/api/notifications/" + created.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 when notification not found")
    void testGetNotificationByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/notifications/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Notification not found with id: 999"));
    }

    @Test
    @DisplayName("Should get notifications by recipient ID")
    void testGetNotificationsByRecipient() throws Exception {
        createTestNotification("student123", "Subject 1");
        createTestNotification("student123", "Subject 2");
        createTestNotification("student456", "Subject 3");

        mockMvc.perform(get("/api/notifications/recipient/student123")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].recipientId", everyItem(is("student123"))));
    }

    @Test
    @DisplayName("Should get unread notifications")
    void testGetUnreadNotifications() throws Exception {
        NotificationDto notif1 = createTestNotification("student123", "Unread 1");
        NotificationDto notif2 = createTestNotification("student123", "Unread 2");
        NotificationDto notif3 = createTestNotification("student123", "Read");

        // Mark one as read
        mockMvc.perform(patch("/api/notifications/" + notif3.getId() + "/read")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/recipient/student123/unread")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].isRead", everyItem(is(false))));
    }

    @Test
    @DisplayName("Should get unread count")
    void testGetUnreadCount() throws Exception {
        createTestNotification("student123", "Unread 1");
        createTestNotification("student123", "Unread 2");
        NotificationDto notif3 = createTestNotification("student123", "Read");

        // Mark one as read
        mockMvc.perform(patch("/api/notifications/" + notif3.getId() + "/read")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/recipient/student123/unread/count")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2));
    }

    @Test
    @DisplayName("Should get notifications by status")
    void testGetNotificationsByStatus() throws Exception {
        NotificationDto notif1 = createTestNotification("student123", "Pending 1");
        NotificationDto notif2 = createTestNotification("student456", "Pending 2");
        NotificationDto notif3 = createTestNotification("student789", "To Send");

        // Send one notification
        mockMvc.perform(patch("/api/notifications/" + notif3.getId() + "/send")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/status/PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("Should get notifications by type")
    void testGetNotificationsByType() throws Exception {
        createTestNotificationWithType("student123", NotificationType.ABSENCE_ALERT);
        createTestNotificationWithType("student456", NotificationType.ABSENCE_ALERT);
        createTestNotificationWithType("student789", NotificationType.WEEKLY_REPORT);

        mockMvc.perform(get("/api/notifications/type/ABSENCE_ALERT")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].type", everyItem(is("ABSENCE_ALERT"))));
    }

    @Test
    @DisplayName("Should mark notification as read")
    void testMarkAsRead() throws Exception {
        NotificationDto created = createTestNotification("student123", "Test Subject");

        mockMvc.perform(patch("/api/notifications/" + created.getId() + "/read")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void testMarkAllAsRead() throws Exception {
        createTestNotification("student123", "Unread 1");
        createTestNotification("student123", "Unread 2");
        createTestNotification("student123", "Unread 3");

        mockMvc.perform(patch("/api/notifications/recipient/student123/read-all")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNoContent());

        // Verify all are read
        mockMvc.perform(get("/api/notifications/recipient/student123/unread/count")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    @DisplayName("Teacher should send notification")
    void testSendNotification() throws Exception {
        NotificationDto created = createTestNotification("student123", "Test Subject");

        mockMvc.perform(patch("/api/notifications/" + created.getId() + "/send")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.sentAt").exists());
    }

    @Test
    @DisplayName("Student should not send notification")
    void testSendNotificationAsStudent() throws Exception {
        NotificationDto created = createTestNotification("student123", "Test Subject");

        mockMvc.perform(patch("/api/notifications/" + created.getId() + "/send")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should delete notification")
    void testDeleteNotification() throws Exception {
        NotificationDto created = createTestNotification("student123", "Test Subject");

        mockMvc.perform(delete("/api/notifications/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/notifications/" + created.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Teacher should not delete notification")
    void testDeleteNotificationAsTeacher() throws Exception {
        NotificationDto created = createTestNotification("student123", "Test Subject");

        mockMvc.perform(delete("/api/notifications/" + created.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    // Helper methods
    private NotificationDto createTestNotification(String recipientId, String subject) throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setRecipientId(recipientId);
        dto.setRecipientType(RecipientType.STUDENT);
        dto.setSubject(subject);
        dto.setMessage("Test message for " + subject);
        dto.setType(NotificationType.ABSENCE_ALERT);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, NotificationDto.class);
    }

    private NotificationDto createTestNotificationWithType(String recipientId, NotificationType type) throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setRecipientId(recipientId);
        dto.setRecipientType(RecipientType.STUDENT);
        dto.setSubject("Subject for " + type);
        dto.setMessage("Message for " + type);
        dto.setType(type);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, NotificationDto.class);
    }
}
