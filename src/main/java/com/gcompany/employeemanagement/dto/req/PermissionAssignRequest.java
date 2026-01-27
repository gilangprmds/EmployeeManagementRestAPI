package com.gcompany.employeemanagement.dto.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionAssignRequest {

    @NotEmpty(message = "At least one permission is required")
    private List<String> permissionNames;
}
