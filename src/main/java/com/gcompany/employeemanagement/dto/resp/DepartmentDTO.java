package com.gcompany.employeemanagement.dto.resp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private Long id;

    @NotBlank(message = "Department code is required")
    @Size(min = 2, max = 20, message = "Code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code must contain only uppercase letters, numbers and underscores")
    private String code;

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private boolean isActive;
    private Integer employeeCount;
    private UserDetailResponse manager;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
