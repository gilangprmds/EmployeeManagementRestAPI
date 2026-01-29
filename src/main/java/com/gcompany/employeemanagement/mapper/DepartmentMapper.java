package com.gcompany.employeemanagement.mapper;

import com.gcompany.employeemanagement.dto.resp.DepartmentDTO;
import com.gcompany.employeemanagement.dto.resp.DepartmentResponse;
import com.gcompany.employeemanagement.dto.resp.UserDetailResponse;
import com.gcompany.employeemanagement.model.Department;
import com.gcompany.employeemanagement.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DepartmentMapper {
    public DepartmentDTO toDTO(Department department) {
        if (department == null) {
            return null;
        }

        UserDetailResponse managerDTO = null;
        if (department.getManager() != null) {
            managerDTO = UserDetailResponse.builder()
                    .id(department.getManager().getId())
                    .fullName(department.getManager().getFullName())
                    .email(department.getManager().getEmail())
                    .roles(new ArrayList<>(department.getManager().getRoleCodes()))
                    .build();
        }

        return DepartmentDTO.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .isActive(department.isActive())
                .employeeCount(department.getEmployees() != null ? department.getEmployees().size() : 0)
                .manager(managerDTO)
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    public DepartmentResponse toResponse(Department department) {
        if (department == null) {
            return null;
        }

        UserDetailResponse managerInfo = null;
        if (department.getManager() != null) {
            managerInfo = UserDetailResponse.builder()
                    .id(department.getManager().getId())
                    .fullName(department.getManager().getFullName())
                    .email(department.getManager().getEmail())
                    .roles(new ArrayList<>(department.getManager().getRoleCodes()))
                    .build();
        }

        return DepartmentResponse.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .isActive(department.isActive())
                .employeeCount(department.getEmployees() != null ? department.getEmployees().size() : 0)
                .manager(managerInfo)
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    public List<DepartmentResponse> toResponseList(List<Department> departments) {
        return departments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Department toEntity(DepartmentDTO dto) {
        if (dto == null) {
            return null;
        }

        return Department.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.isActive())
                .build();
    }

    // For updating entity from request
    public void updateEntityFromDTO(Department department, DepartmentDTO dto) {
        if (dto.getName() != null) {
            department.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            department.setDescription(dto.getDescription());
        }
        department.setActive(dto.isActive());
    }
}
