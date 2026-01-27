package com.gcompany.employeemanagement.mapper;

import com.gcompany.employeemanagement.dto.resp.PermissionResponse;
import com.gcompany.employeemanagement.model.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toPermissionResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

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
                .roleCount(permission.getRoles() != null ? permission.getRoles().size() : 0)
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .createdBy(permission.getCreatedBy())
                .updatedBy(permission.getUpdatedBy())
                .build();
    }
}