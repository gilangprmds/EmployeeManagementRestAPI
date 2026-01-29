package com.gcompany.employeemanagement.dto.resp;

import com.gcompany.employeemanagement.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserStatus status;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
