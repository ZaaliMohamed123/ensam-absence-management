package com.ensam.academicservice.integration;

import com.ensam.academicservice.dto.AcademicClassDto;
import com.ensam.academicservice.dto.ModuleDto;
import com.ensam.academicservice.dto.ScheduleDto;
import com.ensam.academicservice.entities.Schedule.SessionType;
import com.ensam.academicservice.repositories.AcademicClassRepository;
import com.ensam.academicservice.repositories.ModuleRepository;
import com.ensam.academicservice.repositories.ScheduleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Schedule Controller Integration Tests")
class ScheduleControllerIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ScheduleRepository scheduleRepository;
    private ModuleRepository moduleRepository;
    private AcademicClassRepository classRepository;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @Autowired
    public ScheduleControllerIntegrationTest(
            MockMvc mockMvc,
            ScheduleRepository scheduleRepository,
            ModuleRepository moduleRepository,
            AcademicClassRepository classRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.scheduleRepository = scheduleRepository;
        this.moduleRepository = moduleRepository;
        this.classRepository = classRepository;
    }

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        moduleRepository.deleteAll();
        classRepository.deleteAll();

        adminToken = generateToken("admin@ensam.ma", "ROLE_ADMIN");
        teacherToken = generateToken("teacher@ensam.ma", "ROLE_TEACHER");
        studentToken = generateToken("student@ensam.ma", "ROLE_STUDENT");
    }

    @Test
    @DisplayName("Admin should create schedule successfully")
    void testCreateScheduleAsAdmin() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());

        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setClassId(classDto.getId());
        scheduleDto.setModuleId(moduleDto.getId());
        scheduleDto.setTeacherId("teacher123");
        scheduleDto.setDayOfWeek(DayOfWeek.MONDAY);
        scheduleDto.setStartTime(LocalTime.of(8, 0));
        scheduleDto.setEndTime(LocalTime.of(10, 0));
        scheduleDto.setRoom("Amphi A");
        scheduleDto.setSessionType(SessionType.COURSE);
        scheduleDto.setIsRecurring(true);

        String requestJson = objectMapper.writeValueAsString(scheduleDto);

        mockMvc.perform(post("/api/schedules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.teacherId").value("teacher123"))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.room").value("Amphi A"))
                .andExpect(jsonPath("$.sessionType").value("COURSE"))
                .andExpect(jsonPath("$.className").value("GI-2A-1"))
                .andExpect(jsonPath("$.moduleName").value("Base de données"));
    }

    @Test
    @DisplayName("Should fail to create schedule with invalid time")
    void testCreateScheduleInvalidTime() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());

        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setClassId(classDto.getId());
        scheduleDto.setModuleId(moduleDto.getId());
        scheduleDto.setTeacherId("teacher123");
        scheduleDto.setDayOfWeek(DayOfWeek.MONDAY);
        scheduleDto.setStartTime(LocalTime.of(10, 0));
        scheduleDto.setEndTime(LocalTime.of(8, 0)); // End before start
        scheduleDto.setRoom("Amphi A");
        scheduleDto.setSessionType(SessionType.COURSE);
        scheduleDto.setIsRecurring(true);

        String requestJson = objectMapper.writeValueAsString(scheduleDto);

        mockMvc.perform(post("/api/schedules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start time must be before end time"));
    }

    @Test
    @DisplayName("Teacher should not create schedule")
    void testCreateScheduleAsTeacher() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());

        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setClassId(classDto.getId());
        scheduleDto.setModuleId(moduleDto.getId());
        scheduleDto.setTeacherId("teacher123");
        scheduleDto.setDayOfWeek(DayOfWeek.MONDAY);
        scheduleDto.setStartTime(LocalTime.of(8, 0));
        scheduleDto.setEndTime(LocalTime.of(10, 0));
        scheduleDto.setRoom("Amphi A");
        scheduleDto.setSessionType(SessionType.COURSE);
        scheduleDto.setIsRecurring(true);

        String requestJson = objectMapper.writeValueAsString(scheduleDto);

        mockMvc.perform(post("/api/schedules")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("All roles should get schedule by ID")
    void testGetScheduleById() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());
        ScheduleDto scheduleDto = createTestSchedule(classDto.getId(), moduleDto.getId(), DayOfWeek.MONDAY);

        // Test as Student
        mockMvc.perform(get("/api/schedules/" + scheduleDto.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
    }

    @Test
    @DisplayName("Should get schedules by class ID")
    void testGetSchedulesByClassId() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());

        createTestSchedule(classDto.getId(), moduleDto.getId(), DayOfWeek.MONDAY);
        createTestSchedule(classDto.getId(), moduleDto.getId(), DayOfWeek.TUESDAY);

        mockMvc.perform(get("/api/schedules/class/" + classDto.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].classId", everyItem(is(classDto.getId().intValue()))));
    }

    @Test
    @DisplayName("Should get schedules by teacher ID")
    void testGetSchedulesByTeacherId() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());

        createTestSchedule(classDto.getId(), moduleDto.getId(), DayOfWeek.MONDAY);
        createTestSchedule(classDto.getId(), moduleDto.getId(), DayOfWeek.TUESDAY);

        mockMvc.perform(get("/api/schedules/teacher/teacher123")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should get schedules by class ID and day")
    void testGetSchedulesByClassIdAndDay() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());

        createTestSchedule(classDto.getId(), moduleDto.getId(), DayOfWeek.MONDAY);
        createTestSchedule(classDto.getId(), moduleDto.getId(), DayOfWeek.TUESDAY);

        mockMvc.perform(get("/api/schedules/class/" + classDto.getId() + "/day/MONDAY")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    @DisplayName("Admin should update schedule successfully")
    void testUpdateSchedule() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());
        ScheduleDto scheduleDto = createTestSchedule(classDto.getId(), moduleDto.getId(), DayOfWeek.MONDAY);

        ScheduleDto updateDto = new ScheduleDto();
        updateDto.setClassId(classDto.getId());
        updateDto.setModuleId(moduleDto.getId());
        updateDto.setTeacherId("teacher456");
        updateDto.setDayOfWeek(DayOfWeek.WEDNESDAY);
        updateDto.setStartTime(LocalTime.of(10, 0));
        updateDto.setEndTime(LocalTime.of(12, 0));
        updateDto.setRoom("Salle 201");
        updateDto.setSessionType(SessionType.TD);
        updateDto.setIsRecurring(false);

        String requestJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/schedules/" + scheduleDto.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherId").value("teacher456"))
                .andExpect(jsonPath("$.dayOfWeek").value("WEDNESDAY"))
                .andExpect(jsonPath("$.room").value("Salle 201"))
                .andExpect(jsonPath("$.sessionType").value("TD"))
                .andExpect(jsonPath("$.isRecurring").value(false));
    }

    @Test
    @DisplayName("Admin should delete schedule successfully")
    void testDeleteSchedule() throws Exception {
        AcademicClassDto classDto = createTestClass("GI-2A-1", "2A", "Génie Informatique");
        ModuleDto moduleDto = createTestModule("INF201", "Base de données", classDto.getId());
        ScheduleDto scheduleDto = createTestSchedule(classDto.getId(), moduleDto.getId(), DayOfWeek.MONDAY);

        mockMvc.perform(delete("/api/schedules/" + scheduleDto.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/schedules/" + scheduleDto.getId())
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

    private ScheduleDto createTestSchedule(Long classId, Long moduleId, DayOfWeek day) throws Exception {
        ScheduleDto dto = new ScheduleDto();
        dto.setClassId(classId);
        dto.setModuleId(moduleId);
        dto.setTeacherId("teacher123");
        dto.setDayOfWeek(day);
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setRoom("Amphi A");
        dto.setSessionType(SessionType.COURSE);
        dto.setIsRecurring(true);

        String requestJson = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/api/schedules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, ScheduleDto.class);
    }
}
