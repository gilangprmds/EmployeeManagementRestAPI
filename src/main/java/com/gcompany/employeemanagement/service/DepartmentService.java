package com.gcompany.employeemanagement.service;

import com.gcompany.employeemanagement.dto.req.CreateDepartmentRequest;
import com.gcompany.employeemanagement.dto.req.UpdateDepartmentRequest;
import com.gcompany.employeemanagement.dto.req.UpdateDepartmentStatusRequest;
import com.gcompany.employeemanagement.dto.resp.DepartmentDTO;
import com.gcompany.employeemanagement.dto.resp.DepartmentResponse;
import com.gcompany.employeemanagement.dto.resp.PaginatedResponse;
import com.gcompany.employeemanagement.dto.resp.UserDetailResponse;
import com.gcompany.employeemanagement.model.Department;

import java.util.List;

public interface DepartmentService {
    // CRUD Operations
    DepartmentResponse createDepartment(CreateDepartmentRequest request);
    DepartmentResponse getDepartmentById(Long id);
    PaginatedResponse<DepartmentResponse> getAllDepartments(int page, int size, String search);
    DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);
    void deleteDepartment(Long id);
    DepartmentResponse updateDepartmentStatus(Long id, UpdateDepartmentStatusRequest request);

    // Business Logic Operations
    boolean isCodeUnique(String code, Long excludeId);
    boolean hasEmployees(Long departmentId);
    void validateDepartmentDeletion(Long departmentId);

    // Query Operations
    List<DepartmentResponse> getAllActiveDepartments();
    List<DepartmentResponse> getDepartmentsByManager(Long managerId);
    List<DepartmentDTO> getDepartmentStatistics();
    List<UserDetailResponse> getPotentialManagers(Long departmentId);

    // Utility Methods
    Department getDepartmentEntity(Long id);
    void updateDepartmentManager(Long departmentId, Long managerId);
    void removeDepartmentManager(Long departmentId);
}
