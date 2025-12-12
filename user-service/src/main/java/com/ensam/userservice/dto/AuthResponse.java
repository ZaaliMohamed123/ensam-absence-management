package com.ensam.userservice.dto;

import com.ensam.userservice.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;

    public AuthResponse(String token, Long userId, String email, Role role, String firstName, String lastName) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
