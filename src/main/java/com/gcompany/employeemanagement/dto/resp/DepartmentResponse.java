package com.gcompany.employeemanagement.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResponse {
    private Long id;
    private String code;
    private String name;
    private String description;

    @JsonProperty("isActive")
    private boolean isActive;

    private Integer employeeCount;
    private UserDetailResponse manager;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
