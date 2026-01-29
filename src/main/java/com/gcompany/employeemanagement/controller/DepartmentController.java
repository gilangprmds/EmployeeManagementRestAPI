package com.gcompany.employeemanagement.controller;

import com.gcompany.employeemanagement.dto.req.CreateDepartmentRequest;
import com.gcompany.employeemanagement.dto.req.UpdateDepartmentRequest;
import com.gcompany.employeemanagement.dto.req.UpdateDepartmentStatusRequest;
import com.gcompany.employeemanagement.dto.resp.DepartmentDTO;
import com.gcompany.employeemanagement.dto.resp.DepartmentResponse;
import com.gcompany.employeemanagement.dto.resp.PaginatedResponse;
import com.gcompany.employeemanagement.dto.resp.UserDetailResponse;
import com.gcompany.employeemanagement.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "APIs for managing departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @Operation(summary = "Get all departments", description = "Retrieve paginated list of departments with optional search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved departments"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or hasPermission('department', 'read')")
    public ResponseEntity<PaginatedResponse<DepartmentResponse>> getAllDepartments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Search term for name or code") @RequestParam(required = false) String search) {

        PaginatedResponse<DepartmentResponse> response = departmentService.getAllDepartments(page, size, search);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get department by ID", description = "Retrieve a specific department by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved department"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or hasPermission('department', 'read')")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @Parameter(description = "Department ID") @PathVariable Long id) {

        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new department", description = "Create a new department in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Department created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Department code already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasPermission('department', 'create')")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {

        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update department", description = "Update an existing department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasPermission('department', 'update')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @Parameter(description = "Department ID") @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {

        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete department", description = "Delete a department from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete department with employees"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasPermission('department', 'delete')")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "Department ID") @PathVariable Long id) {

        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update department status", description = "Activate or deactivate a department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasPermission('department', 'update')")
    public ResponseEntity<DepartmentResponse> updateDepartmentStatus(
            @Parameter(description = "Department ID") @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentStatusRequest request) {

        DepartmentResponse response = departmentService.updateDepartmentStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get active departments", description = "Retrieve list of all active departments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved departments"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/active")
    public ResponseEntity<List<DepartmentResponse>> getActiveDepartments() {
        List<DepartmentResponse> response = departmentService.getAllActiveDepartments();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get department statistics", description = "Retrieve department statistics including employee counts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<List<DepartmentDTO>> getDepartmentStatistics() {
        List<DepartmentDTO> response = departmentService.getDepartmentStatistics();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get potential managers", description = "Retrieve list of users who can be assigned as department managers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved potential managers"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/potential-managers")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<List<UserDetailResponse>> getPotentialManagers(
            @Parameter(description = "Department ID for which to find managers")
            @RequestParam(required = false) Long departmentId) {

        List<UserDetailResponse> response = departmentService.getPotentialManagers(departmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check department code uniqueness", description = "Check if a department code is unique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked uniqueness"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/check-code-unique")
    public ResponseEntity<Boolean> checkCodeUnique(
            @Parameter(description = "Department code to check") @RequestParam String code,
            @Parameter(description = "Department ID to exclude (for updates)")
            @RequestParam(required = false) Long excludeId) {

        boolean isUnique = departmentService.isCodeUnique(code, excludeId);
        return ResponseEntity.ok(isUnique);
    }

    @Operation(summary = "Validate department deletion", description = "Check if a department can be deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation successful"),
            @ApiResponse(responseCode = "400", description = "Cannot delete department"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}/validate-deletion")
    @PreAuthorize("hasRole('ADMIN') or hasPermission('department', 'delete')")
    public ResponseEntity<Void> validateDepartmentDeletion(
            @Parameter(description = "Department ID") @PathVariable Long id) {

        departmentService.validateDepartmentDeletion(id);
        return ResponseEntity.ok().build();
    }
}
