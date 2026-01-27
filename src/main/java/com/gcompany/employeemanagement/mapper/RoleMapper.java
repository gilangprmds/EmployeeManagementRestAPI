package com.gcompany.employeemanagement.mapper;

import com.gcompany.employeemanagement.dto.req.RoleUpdateRequest;
import com.gcompany.employeemanagement.dto.resp.PermissionResponse;
import com.gcompany.employeemanagement.dto.resp.RoleResponse;
import com.gcompany.employeemanagement.model.Permission;
import com.gcompany.employeemanagement.model.Role;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .description(role.getDescription())
                .type(role.getType())
                .isDefault(role.isDefaultRole())
                .isSystem(role.isSystem())
                .isActive(role.isActive())
                .priority(role.getPriority())
                .parentRoleCode(role.getParentRole() != null ? role.getParentRole().getCode() : null)
                .parentRoleName(role.getParentRole() != null ? role.getParentRole().getName() : null)
                .permissions(role.getPermissions().stream()
                        .map(this::toPermissionResponse)
                        .collect(Collectors.toList()))
                .userCount(role.getUsers() != null ? role.getUsers().size() : 0)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }

    private PermissionResponse toPermissionResponse(
            Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction())
                .scope(permission.getScope())
                .sensitive(permission.isSensitive())
                .active(permission.isActive())
                .category(permission.getCategory())
                .build();
    }

    public void updateRoleFromRequest(Role role, RoleUpdateRequest request) {
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            role.setType(request.getType());
        }
        if (request.getIsDefault() != null) {
            role.setDefaultRole(request.getIsDefault());
        }
        if (request.getIsActive() != null) {
            role.setActive(request.getIsActive());
        }
        if (request.getPriority() != null) {
            role.setPriority(request.getPriority());
        }
    }
}