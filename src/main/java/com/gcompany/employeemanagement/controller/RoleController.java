package com.gcompany.employeemanagement.controller;

import com.gcompany.employeemanagement.dto.req.PermissionAssignRequest;
import com.gcompany.employeemanagement.dto.req.RoleCreateRequest;
import com.gcompany.employeemanagement.dto.req.RoleUpdateRequest;
import com.gcompany.employeemanagement.dto.resp.PaginatedResponse;
import com.gcompany.employeemanagement.dto.resp.RoleResponse;
import com.gcompany.employeemanagement.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing roles")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role", description = "Create a new role. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Get role details by ID. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> getRoleById(
            @Parameter(description = "Role ID") @PathVariable Long id) {
        RoleResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get role by code", description = "Get role details by code. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<RoleResponse> getRoleByCode(
            @Parameter(description = "Role code") @PathVariable String code) {
        RoleResponse response = roleService.getRoleByCode(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all roles (paginated)", description = "Get a paginated list of roles. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<PaginatedResponse<RoleResponse>> getAllRoles(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "priority") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        PaginatedResponse<RoleResponse> response = roleService.getAllRoles(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Update role details. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> updateRole(
            @Parameter(description = "Role ID") @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Delete a role. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "Role ID") @PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permissions")
    @Operation(summary = "Assign permissions to role", description = "Assign permissions to a role. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> assignPermissionsToRole(
            @Parameter(description = "Role ID") @PathVariable Long id,
            @Valid @RequestBody PermissionAssignRequest request) {
        RoleResponse response = roleService.assignPermissionsToRole(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/permissions/{permissionName}")
    @Operation(summary = "Add permission to role", description = "Add a single permission to a role. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> addPermissionToRole(
            @Parameter(description = "Role ID") @PathVariable Long id,
            @Parameter(description = "Permission name") @PathVariable String permissionName) {
        RoleResponse response = roleService.addPermissionToRole(id, permissionName);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/permissions/{permissionName}")
    @Operation(summary = "Remove permission from role", description = "Remove a permission from a role. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> removePermissionFromRole(
            @Parameter(description = "Role ID") @PathVariable Long id,
            @Parameter(description = "Permission name") @PathVariable String permissionName) {
        RoleResponse response = roleService.removePermissionFromRole(id, permissionName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate role", description = "Activate a role. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> activateRole(
            @Parameter(description = "Role ID") @PathVariable Long id) {
        RoleResponse response = roleService.activateRole(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate role", description = "Deactivate a role. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> deactivateRole(
            @Parameter(description = "Role ID") @PathVariable Long id) {
        RoleResponse response = roleService.deactivateRole(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search roles", description = "Search roles by name or code. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<RoleResponse>> searchRoles(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        List<RoleResponse> response = roleService.searchRoles(keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active roles", description = "Get all active roles. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<RoleResponse>> getActiveRoles() {
        List<RoleResponse> response = roleService.getActiveRoles();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/default")
    @Operation(summary = "Get default roles", description = "Get all default roles. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<RoleResponse>> getDefaultRoles() {
        List<RoleResponse> response = roleService.getDefaultRoles();
        return ResponseEntity.ok(response);
    }
}