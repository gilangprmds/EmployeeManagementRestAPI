package com.gcompany.employeemanagement.dto.resp;

import com.gcompany.employeemanagement.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    private Long id;
    private String email;
    private String fullName;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserStatus status;
    private String profileImageUrl;
    private String department;

    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private Boolean enabled;

    private List<String> roles;
    private List<String> permissions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}