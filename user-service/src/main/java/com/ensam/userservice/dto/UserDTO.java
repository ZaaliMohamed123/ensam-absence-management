package com.ensam.userservice.dto;

import com.ensam.userservice.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;
    private boolean active;
    private LocalDateTime createdAt;

    // Role-specific data
    private String studentNumber;
    private Long classId;
    private Integer enrollmentYear;
    private String teacherCode;
    private String department;
    private String adminCode;
}
