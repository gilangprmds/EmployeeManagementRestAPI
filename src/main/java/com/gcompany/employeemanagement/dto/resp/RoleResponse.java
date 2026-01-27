package com.gcompany.employeemanagement.dto.resp;

import com.gcompany.employeemanagement.enums.RoleType;
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
public class RoleResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private RoleType type;
    private Boolean isDefault;
    private Boolean isSystem;
    private Boolean isActive;
    private Integer priority;

    private String parentRoleCode;
    private String parentRoleName;

    private List<PermissionResponse> permissions;
    private Integer userCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}

