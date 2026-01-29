package com.gcompany.employeemanagement.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateDepartmentStatusRequest {
    @NotNull(message = "Status is required")
    private Boolean isActive;
}
