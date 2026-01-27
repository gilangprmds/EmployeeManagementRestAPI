package com.gcompany.employeemanagement.controller;

import com.gcompany.employeemanagement.dto.resp.PaginatedResponse;
import com.gcompany.employeemanagement.dto.resp.PermissionResponse;
import com.gcompany.employeemanagement.enums.ActionType;
import com.gcompany.employeemanagement.enums.ResourceType;
import com.gcompany.employeemanagement.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "APIs for managing permissions")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID", description = "Get permission details by ID. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> getPermissionById(
            @Parameter(description = "Permission ID") @PathVariable Long id) {
        PermissionResponse response = permissionService.getPermissionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get permission by name", description = "Get permission details by name. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> getPermissionByName(
            @Parameter(description = "Permission name") @PathVariable String name) {
        PermissionResponse response = permissionService.getPermissionByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all permissions (paginated)", description = "Get a paginated list of permissions. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<PermissionResponse>> getAllPermissions(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "resource") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        PaginatedResponse<PermissionResponse> response = permissionService.getAllPermissions(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resource/{resource}")
    @Operation(summary = "Get permissions by resource", description = "Get all permissions for a specific resource. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByResource(
            @Parameter(description = "Resource type") @PathVariable ResourceType resource) {
        List<PermissionResponse> response = permissionService.getPermissionsByResource(resource);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get permissions by action", description = "Get all permissions for a specific action. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByAction(
            @Parameter(description = "Action type") @PathVariable ActionType action) {
        List<PermissionResponse> response = permissionService.getPermissionsByAction(action);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get permissions by category", description = "Get all permissions for a specific category. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByCategory(
            @Parameter(description = "Category") @PathVariable String category) {
        List<PermissionResponse> response = permissionService.getPermissionsByCategory(category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active permissions", description = "Get all active permissions. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<PermissionResponse>> getActivePermissions() {
        List<PermissionResponse> response = permissionService.getActivePermissions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{roleCode}")
    @Operation(summary = "Get permissions by role", description = "Get all permissions for a specific role. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByRole(
            @Parameter(description = "Role code") @PathVariable String roleCode) {
        List<PermissionResponse> response = permissionService.getPermissionsByRole(roleCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new permission", description = "Create a new permission. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> createPermission(
            @RequestParam ResourceType resource,
            @RequestParam ActionType action,
            @RequestParam(required = false) String scope) {
        PermissionResponse response = permissionService.createPermission(resource, action, scope);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update permission", description = "Update permission details. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> updatePermission(
            @Parameter(description = "Permission ID") @PathVariable Long id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean sensitive) {
        PermissionResponse response = permissionService.updatePermission(id, description, active, sensitive);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate permission", description = "Activate a permission. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> activatePermission(
            @Parameter(description = "Permission ID") @PathVariable Long id) {
        PermissionResponse response = permissionService.activatePermission(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate permission", description = "Deactivate a permission. Requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> deactivatePermission(
            @Parameter(description = "Permission ID") @PathVariable Long id) {
        PermissionResponse response = permissionService.deactivatePermission(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search permissions", description = "Search permissions by name, description, or resource. Requires ADMIN or HR role.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<PermissionResponse>> searchPermissions(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        List<PermissionResponse> response = permissionService.searchPermissions(keyword);
        return ResponseEntity.ok(response);
    }
}
