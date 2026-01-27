package com.gcompany.employeemanagement.dto.req;

import com.gcompany.employeemanagement.enums.RoleType;
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
public class RoleUpdateRequest {

    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    private String name;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    private RoleType type;
    private Boolean isDefault;
    private Boolean isActive;
    private Integer priority;

    private String parentRoleCode;
    private List<String> permissionNames;
}

