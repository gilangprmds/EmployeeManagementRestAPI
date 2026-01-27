package com.gcompany.employeemanagement.controller;

import com.gcompany.employeemanagement.dto.Response;
import com.gcompany.employeemanagement.dto.req.UserCreateRequest;
import com.gcompany.employeemanagement.dto.req.UserRoleAssignRequest;
import com.gcompany.employeemanagement.dto.req.UserUpdateRequest;
import com.gcompany.employeemanagement.dto.resp.PaginatedResponse;
import com.gcompany.employeemanagement.dto.resp.UserDetailResponse;
import com.gcompany.employeemanagement.service.UserService2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController2 {

    private final UserService2 userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new user", description = "Create a new user. Requires ADMIN role.")
    public ResponseEntity<UserDetailResponse> createUser(@Valid @ModelAttribute UserCreateRequest request) {
        UserDetailResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user details by ID. Requires ADMIN role or being the user themselves.")
    public ResponseEntity<UserDetailResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserDetailResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all users (paginated)", description = "Get a paginated list of users. Requires ADMIN role.")
    public ResponseEntity<?> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by role") @RequestParam(required = false) String role,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status) {
        PaginatedResponse<UserDetailResponse> response = userService.getAllUsers(page, size, sortBy, sortDir, name, role, status);
        Response<PaginatedResponse<UserDetailResponse>> resp = new Response<>();
        resp.setData(response);
        resp.setMessage("Users retrieved successfully");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user details. Requires ADMIN role or being the user themselves.")
    public ResponseEntity<UserDetailResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @ModelAttribute UserUpdateRequest request) {
        UserDetailResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user. Requires ADMIN role and cannot delete self.")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "Assign roles to user", description = "Assign roles to a user. Requires ADMIN role.")
    public ResponseEntity<UserDetailResponse> assignRolesToUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UserRoleAssignRequest request) {
        UserDetailResponse response = userService.updateUserRoles(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Activate a user account. Requires ADMIN role.")
    public ResponseEntity<UserDetailResponse> activateUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserDetailResponse response = userService.activateUser(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivate a user account. Requires ADMIN role.")
    public ResponseEntity<UserDetailResponse> deactivateUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserDetailResponse response = userService.deactivateUser(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/lock")
    @Operation(summary = "Lock user", description = "Lock a user account. Requires ADMIN role.")
    public ResponseEntity<UserDetailResponse> lockUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserDetailResponse response = userService.lockUser(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock user", description = "Unlock a user account. Requires ADMIN role.")
    public ResponseEntity<UserDetailResponse> unlockUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserDetailResponse response = userService.unlockUser(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/change-password")
    @Operation(summary = "Change user password", description = "Change user password. Requires ADMIN role or being the user themselves.")
    public ResponseEntity<UserDetailResponse> changeUserPassword(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestParam String newPassword) {
        UserDetailResponse response = userService.changeUserPassword(id, newPassword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name", description = "Search users by full name. Requires ADMIN or HR role.")
    public ResponseEntity<List<UserDetailResponse>> searchUsers(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        List<UserDetailResponse> response = userService.searchUsers(keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{roleCode}")
    @Operation(summary = "Get users by role", description = "Get all users with a specific role. Requires ADMIN role.")
    public ResponseEntity<List<UserDetailResponse>> getUsersByRole(
            @Parameter(description = "Role code") @PathVariable String roleCode) {
        List<UserDetailResponse> response = userService.getUsersByRole(roleCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active users", description = "Get all active users. Requires ADMIN or HR role.")
    public ResponseEntity<List<UserDetailResponse>> getActiveUsers() {
        List<UserDetailResponse> response = userService.getActiveUsers();
        return ResponseEntity.ok(response);
    }
}