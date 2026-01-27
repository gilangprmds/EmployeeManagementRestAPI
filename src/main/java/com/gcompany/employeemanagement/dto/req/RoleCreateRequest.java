package com.gcompany.employeemanagement.dto.req;

import com.gcompany.employeemanagement.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequest {

    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Role code is required")
    @Size(min = 2, max = 20, message = "Role code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role code must contain only uppercase letters and underscores")
    private String code;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    private RoleType type;
    private Boolean isDefault;
    private Boolean isSystem;
    private Integer priority;

    private String parentRoleCode;
    private List<String> permissionNames;
}