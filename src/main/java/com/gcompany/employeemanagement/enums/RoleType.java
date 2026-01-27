package com.gcompany.employeemanagement.enums;

public enum RoleType {
    SYSTEM,     // Role sistem yang dibuat otomatis (ADMIN, USER, etc.)
    BUSINESS,   // Role bisnis (HR_MANAGER, DEPARTMENT_HEAD)
    CUSTOM,     // Role kustom yang dibuat manual
    FUNCTIONAL  // Role fungsional (APPROVER, REVIEWER)
}
