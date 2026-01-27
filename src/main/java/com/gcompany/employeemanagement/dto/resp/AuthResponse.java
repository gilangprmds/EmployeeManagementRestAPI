package com.gcompany.employeemanagement.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType = "Bearer";

    private Long expiresIn;

    private Long userId;
    private String email;
    private String fullName;
    private String profileImageUrl;
    private String department;
    private List<String> roles;
    private List<String> permissions;
}
