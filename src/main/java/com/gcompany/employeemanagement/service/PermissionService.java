package com.gcompany.employeemanagement.service;

import com.gcompany.employeemanagement.dto.resp.PaginatedResponse;
import com.gcompany.employeemanagement.dto.resp.PermissionResponse;
import com.gcompany.employeemanagement.enums.ActionType;
import com.gcompany.employeemanagement.enums.ResourceType;
import com.gcompany.employeemanagement.exception.BusinessRuleException;
import com.gcompany.employeemanagement.exception.ResourceNotFoundException;
import com.gcompany.employeemanagement.mapper.PermissionMapper;
import com.gcompany.employeemanagement.model.Permission;
import com.gcompany.employeemanagement.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final SecurityService securityService;

    // ========== Read Operations ==========

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse getPermissionById(Long permissionId) {
        log.info("Fetching permission by ID: {}", permissionId);

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        return permissionMapper.toPermissionResponse(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse getPermissionByName(String permissionName) {
        log.info("Fetching permission by name: {}", permissionName);

        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", permissionName));

        return permissionMapper.toPermissionResponse(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginatedResponse<PermissionResponse> getAllPermissions(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching all permissions - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Permission> permissionsPage = permissionRepository.findAll(pageable);

        List<PermissionResponse> permissionResponses = permissionsPage.getContent().stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<PermissionResponse>builder()
                .content(permissionResponses)
                .page(permissionsPage.getNumber())
                .size(permissionsPage.getSize())
                .totalElements(permissionsPage.getTotalElements())
                .totalPages(permissionsPage.getTotalPages())
                .last(permissionsPage.isLast())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<PermissionResponse> getPermissionsByResource(ResourceType resource) {
        log.info("Fetching permissions for resource: {}", resource);

        List<Permission> permissions = permissionRepository.findByResource(resource);

        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<PermissionResponse> getPermissionsByAction(ActionType action) {
        log.info("Fetching permissions for action: {}", action);

        List<Permission> permissions = permissionRepository.findByAction(action);

        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<PermissionResponse> getPermissionsByCategory(String category) {
        log.info("Fetching permissions for category: {}", category);

        List<Permission> permissions = permissionRepository.findByCategory(category);

        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<PermissionResponse> getActivePermissions() {
        log.info("Fetching all active permissions");

        List<Permission> permissions = permissionRepository.findByActive(true);

        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<PermissionResponse> getPermissionsByRole(String roleCode) {
        log.info("Fetching permissions for role: {}", roleCode);

        List<Permission> permissions = permissionRepository.findByRoleCode(roleCode);

        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    // ========== Create/Update Operations ==========

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PermissionResponse createPermission(ResourceType resource, ActionType action, String scope) {
        log.info("Creating permission: {} {} {}", resource, action, scope);

        // Check if permission already exists
        if (scope != null && !scope.isEmpty()) {
            if (permissionRepository.findByResourceAndActionAndScope(resource, action, scope).isPresent()) {
                throw new BusinessRuleException(
                        String.format("Permission already exists: %s %s %s", resource, action, scope));
            }
        } else {
            if (permissionRepository.existsByResourceAndAction(resource, action)) {
                throw new BusinessRuleException(
                        String.format("Permission already exists: %s %s", resource, action));
            }
        }

        Permission permission = Permission.of(resource, action, scope);
        permission.setCreatedBy(securityService.getCurrentUserId());

        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission created successfully: {}", savedPermission.getName());

        return permissionMapper.toPermissionResponse(savedPermission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PermissionResponse updatePermission(Long permissionId, String description, Boolean active, Boolean sensitive) {
        log.info("Updating permission: {}", permissionId);

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        if (description != null) {
            permission.setDescription(description);
        }
        if (active != null) {
            permission.setActive(active);
        }
        if (sensitive != null) {
            permission.setSensitive(sensitive);
        }

        permission.setUpdatedBy(securityService.getCurrentUserId());

        Permission updatedPermission = permissionRepository.save(permission);
        log.info("Permission updated successfully: {}", updatedPermission.getName());

        return permissionMapper.toPermissionResponse(updatedPermission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PermissionResponse activatePermission(Long permissionId) {
        log.info("Activating permission: {}", permissionId);

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        permission.setActive(true);
        permission.setUpdatedBy(securityService.getCurrentUserId());

        Permission updatedPermission = permissionRepository.save(permission);
        log.info("Permission activated: {}", updatedPermission.getName());

        return permissionMapper.toPermissionResponse(updatedPermission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PermissionResponse deactivatePermission(Long permissionId) {
        log.info("Deactivating permission: {}", permissionId);

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        permission.setActive(false);
        permission.setUpdatedBy(securityService.getCurrentUserId());

        Permission updatedPermission = permissionRepository.save(permission);
        log.info("Permission deactivated: {}", updatedPermission.getName());

        return permissionMapper.toPermissionResponse(updatedPermission);
    }

    // ========== Search Operations ==========

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<PermissionResponse> searchPermissions(String keyword) {
        log.info("Searching permissions with keyword: {}", keyword);

        // Search by name, description, or resource
        List<Permission> permissions = permissionRepository.findAll().stream()
                .filter(permission ->
                        permission.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                (permission.getDescription() != null &&
                                        permission.getDescription().toLowerCase().contains(keyword.toLowerCase())) ||
                                permission.getResource().name().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    // ========== Helper Methods ==========

    public boolean permissionExists(String permissionName) {
        return permissionRepository.existsByName(permissionName);
    }

    public List<String> getAllPermissionNames() {
        return permissionRepository.findAll().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }
}