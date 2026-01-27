package com.gcompany.employeemanagement.service;

import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }

        // Jika principal bukan User (misalnya String "anonymousUser"), coba load dari repository
        if (principal instanceof String email) {
            return userRepository.findByEmail(email).orElse(null);
        }

        return null;
    }

    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public boolean isSelf(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(targetUserId);
    }

    public boolean hasRole(String roleCode) {
        User user = getCurrentUser();
        return user != null && user.hasRole(roleCode);
    }

    public boolean hasPermission(String permissionName) {
        User user = getCurrentUser();
        return user != null && user.hasPermission(permissionName);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isHR() {
        return hasRole("HR") || hasRole("HR_MANAGER") || hasRole("HR_STAFF");
    }

    public boolean isManager() {
        return hasRole("MANAGER") || hasRole("DEPT_MANAGER") || hasRole("TEAM_LEADER");
    }

    public boolean isEmployee() {
        return hasRole("EMPLOYEE");
    }

    // Method untuk check hierarchical access
    public boolean canAccessUserData(Long targetUserId) {
        if (isAdmin()) {
            return true;
        }

        if (isSelf(targetUserId)) {
            return true;
        }

        // HR can access all user data
        if (isHR()) {
            return true;
        }

        // Manager can access their team's data
        if (isManager()) {
            // TODO: Implement logic to check if target user is under current user's team
            // This would require organizational hierarchy data
            return false;
        }

        return false;
    }

    public boolean canAccessDepartmentData(String departmentCode) {
        if (isAdmin() || isHR()) {
            return true;
        }

        // TODO: Implement logic to check if current user belongs to the department
        // or manages the department

        return false;
    }
}