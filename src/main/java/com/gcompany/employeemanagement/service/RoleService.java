package com.gcompany.employeemanagement.service;

import com.gcompany.employeemanagement.dto.req.PermissionAssignRequest;
import com.gcompany.employeemanagement.dto.req.RoleCreateRequest;
import com.gcompany.employeemanagement.dto.req.RoleUpdateRequest;
import com.gcompany.employeemanagement.dto.resp.PaginatedResponse;
import com.gcompany.employeemanagement.dto.resp.RoleResponse;
import com.gcompany.employeemanagement.enums.RoleType;
import com.gcompany.employeemanagement.exception.BusinessRuleException;
import com.gcompany.employeemanagement.exception.ResourceNotFoundException;
import com.gcompany.employeemanagement.mapper.RoleMapper;
import com.gcompany.employeemanagement.model.Permission;
import com.gcompany.employeemanagement.model.Role;
import com.gcompany.employeemanagement.repository.PermissionRepository;
import com.gcompany.employeemanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final SecurityService securityService;

    // ========== CRUD Operations ==========

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse createRole(RoleCreateRequest request) {
        log.info("Creating role with code: {}", request.getCode());

        // Check if role code already exists
        if (roleRepository.existsByCode(request.getCode())) {
            throw new BusinessRuleException("Role code already exists: " + request.getCode());
        }

        // Check if role name already exists
        if (roleRepository.existsByName(request.getName())) {
            throw new BusinessRuleException("Role name already exists: " + request.getName());
        }

        // Create role
        Role role = Role.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .type(request.getType() != null ? request.getType() : RoleType.BUSINESS)
                .defaultRole(request.getIsDefault() != null ? request.getIsDefault() : false)
                .system(request.getIsSystem() != null ? request.getIsSystem() : false)
                .active(true)
                .priority(request.getPriority() != null ? request.getPriority() : 10)
                .build();

        // Set parent role if provided
        if (request.getParentRoleCode() != null && !request.getParentRoleCode().isEmpty()) {
            Role parentRole = roleRepository.findByCode(request.getParentRoleCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "code", request.getParentRoleCode()));
            role.setParentRole(parentRole);
        }

        // Assign permissions if provided
        if (request.getPermissionNames() != null && !request.getPermissionNames().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllByNameIn(request.getPermissionNames());
            if (permissions.size() != request.getPermissionNames().size()) {
                throw new BusinessRuleException("Some permissions not found");
            }
            role.setPermissions(new HashSet<>(permissions));
        }

        // Set created by
        role.setCreatedBy(securityService.getCurrentUserId());

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully: {}", savedRole.getCode());

        return roleMapper.toRoleResponse(savedRole);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse getRoleById(Long roleId) {
        log.info("Fetching role by ID: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        return roleMapper.toRoleResponse(role);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public RoleResponse getRoleByCode(String roleCode) {
        log.info("Fetching role by code: {}", roleCode);

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", roleCode));

        return roleMapper.toRoleResponse(role);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public PaginatedResponse<RoleResponse> getAllRoles(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching all roles - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Role> rolesPage = roleRepository.findAll(pageable);

        List<RoleResponse> roleResponses = rolesPage.getContent().stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<RoleResponse>builder()
                .content(roleResponses)
                .page(rolesPage.getNumber())
                .size(rolesPage.getSize())
                .totalElements(rolesPage.getTotalElements())
                .totalPages(rolesPage.getTotalPages())
                .last(rolesPage.isLast())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse updateRole(Long roleId, RoleUpdateRequest request) {
        log.info("Updating role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // Prevent updating system roles
        if (role.isSystem()) {
            throw new BusinessRuleException("Cannot update system role: " + role.getCode());
        }

        // Update role fields
        roleMapper.updateRoleFromRequest(role, request);

        // Update parent role if provided
        if (request.getParentRoleCode() != null) {
            if (request.getParentRoleCode().isEmpty()) {
                role.setParentRole(null);
            } else {
                Role parentRole = roleRepository.findByCode(request.getParentRoleCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "code", request.getParentRoleCode()));

                // Prevent circular reference
                if (isCircularReference(role, parentRole)) {
                    throw new BusinessRuleException("Circular reference detected in role hierarchy");
                }

                role.setParentRole(parentRole);
            }
        }

        // Update permissions if provided
        if (request.getPermissionNames() != null) {
            List<Permission> permissions = permissionRepository.findAllByNameIn(request.getPermissionNames());
            if (permissions.size() != request.getPermissionNames().size()) {
                throw new BusinessRuleException("Some permissions not found");
            }
            role.setPermissions(new HashSet<>(permissions));
        }

        // Set updated by
        role.setUpdatedBy(securityService.getCurrentUserId());

        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully: {}", updatedRole.getCode());

        return roleMapper.toRoleResponse(updatedRole);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRole(Long roleId) {
        log.info("Deleting role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // Prevent deleting system roles
        if (role.isSystem()) {
            throw new BusinessRuleException("Cannot delete system role: " + role.getCode());
        }

        // Check if role has users assigned
        if (!role.getUsers().isEmpty()) {
            throw new BusinessRuleException(
                    String.format("Cannot delete role %s because it has %d users assigned",
                            role.getCode(), role.getUsers().size()));
        }

        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", roleId);
    }

    // ========== Permission Management ==========

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse assignPermissionsToRole(Long roleId, PermissionAssignRequest request) {
        log.info("Assigning permissions to role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        List<Permission> permissions = permissionRepository.findAllByNameIn(request.getPermissionNames());
        if (permissions.size() != request.getPermissionNames().size()) {
            throw new BusinessRuleException("Some permissions not found");
        }

        role.setPermissions(new HashSet<>(permissions));
        role.setUpdatedBy(securityService.getCurrentUserId());

        Role updatedRole = roleRepository.save(role);
        log.info("Permissions assigned to role: {}", updatedRole.getCode());

        return roleMapper.toRoleResponse(updatedRole);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse addPermissionToRole(Long roleId, String permissionName) {
        log.info("Adding permission {} to role: {}", permissionName, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", permissionName));

        role.addPermission(permission);
        role.setUpdatedBy(securityService.getCurrentUserId());

        Role updatedRole = roleRepository.save(role);
        log.info("Permission added to role: {}", updatedRole.getCode());

        return roleMapper.toRoleResponse(updatedRole);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse removePermissionFromRole(Long roleId, String permissionName) {
        log.info("Removing permission {} from role: {}", permissionName, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", permissionName));

        role.removePermission(permission);
        role.setUpdatedBy(securityService.getCurrentUserId());

        Role updatedRole = roleRepository.save(role);
        log.info("Permission removed from role: {}", updatedRole.getCode());

        return roleMapper.toRoleResponse(updatedRole);
    }

    // ========== Business Operations ==========

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse activateRole(Long roleId) {
        log.info("Activating role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        role.setActive(true);
        role.setUpdatedBy(securityService.getCurrentUserId());

        Role updatedRole = roleRepository.save(role);
        log.info("Role activated: {}", updatedRole.getCode());

        return roleMapper.toRoleResponse(updatedRole);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse deactivateRole(Long roleId) {
        log.info("Deactivating role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // Prevent deactivating system roles
        if (role.isSystem()) {
            throw new BusinessRuleException("Cannot deactivate system role: " + role.getCode());
        }

        role.setActive(false);
        role.setUpdatedBy(securityService.getCurrentUserId());

        Role updatedRole = roleRepository.save(role);
        log.info("Role deactivated: {}", updatedRole.getCode());

        return roleMapper.toRoleResponse(updatedRole);
    }

    // ========== Search Operations ==========

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<RoleResponse> searchRoles(String keyword) {
        log.info("Searching roles with keyword: {}", keyword);

        // Search by name or code
        List<Role> roles = roleRepository.findAll().stream()
                .filter(role -> role.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        role.getCode().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        return roles.stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<RoleResponse> getActiveRoles() {
        log.info("Fetching all active roles");

        List<Role> roles = roleRepository.findByActive(true);

        return roles.stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<RoleResponse> getDefaultRoles() {
        log.info("Fetching default roles");

        List<Role> roles = roleRepository.findByDefaultRole(true);

        return roles.stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    // ========== Helper Methods ==========

    private boolean isCircularReference(Role role, Role potentialParent) {
        // Check if setting potentialParent as parent would create a circular reference
        Role current = potentialParent;
        while (current != null) {
            if (current.getId().equals(role.getId())) {
                return true;
            }
            current = current.getParentRole();
        }
        return false;
    }

    public boolean roleExists(String roleCode) {
        return roleRepository.existsByCode(roleCode);
    }
}
