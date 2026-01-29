package com.gcompany.employeemanagement.service.impl;

import com.gcompany.employeemanagement.dto.req.CreateDepartmentRequest;
import com.gcompany.employeemanagement.dto.req.UpdateDepartmentRequest;
import com.gcompany.employeemanagement.dto.req.UpdateDepartmentStatusRequest;
import com.gcompany.employeemanagement.dto.resp.DepartmentDTO;
import com.gcompany.employeemanagement.dto.resp.DepartmentResponse;
import com.gcompany.employeemanagement.dto.resp.PaginatedResponse;
import com.gcompany.employeemanagement.dto.resp.UserDetailResponse;
import com.gcompany.employeemanagement.exception.BusinessRuleException;
import com.gcompany.employeemanagement.exception.ResourceNotFoundException;
import com.gcompany.employeemanagement.mapper.DepartmentMapper;
import com.gcompany.employeemanagement.model.Department;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.DepartmentRepository;
import com.gcompany.employeemanagement.repository.UserRepository;
import com.gcompany.employeemanagement.service.DepartmentService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DepartmentMapper departmentMapper;

    private static final List<String> PROTECTED_DEPARTMENTS = List.of("HR", "IT", "ADMIN", "FINANCE");

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating new department with code: {}", request.getCode());

        // Validate unique department code
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BusinessRuleException("Department code already exists: " + request.getCode());
        }

        // Build department entity
        Department department = Department.builder()
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName().trim())
                .description(request.getDescription())
                .isActive(request.isActive())
                .build();

        // Set manager if provided
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(Long.parseLong(request.getManagerId()))
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getManagerId()));

            // Validate manager is active
            if (!manager.isActive()) {
                throw new ValidationException("Cannot assign inactive user as department manager");
            }

            department.setManager(manager);
        }

        // Save department
        Department savedDepartment = departmentRepository.save(department);
        log.info("Department created successfully: {}", savedDepartment.getCode());

        return departmentMapper.toResponse(savedDepartment);
    }

    @Override
    @Cacheable(value = "departments", key = "#id")
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        log.debug("Fetching department by id: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        return departmentMapper.toResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<DepartmentResponse> getAllDepartments(int page, int size, String search) {
        log.debug("Fetching departments page: {}, size: {}, search: {}", page, size, search);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Department> departmentPage;

        if (search != null && !search.trim().isEmpty()) {
            departmentPage = departmentRepository.searchDepartments(search.trim(), false, pageable);
        } else {
            departmentPage = departmentRepository.findAll(pageable);
        }

        Page<DepartmentResponse> responsePage = departmentPage.map(departmentMapper::toResponse);
        return PaginatedResponse.of(responsePage);
    }

    @Override
    @CacheEvict(value = "departments", key = "#id")
    @Transactional
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        log.info("Updating department id: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Update fields if provided
        if (request.getName() != null) {
            department.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            department.setActive(request.getIsActive());
        }

        // Update manager if provided
        if (request.getManagerId() != null) {

            // Set new manager
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getManagerId()));

            if (!manager.isActive()) {
                throw new ValidationException("Cannot assign inactive user as department manager");
            }

            department.setManager(manager);

        }

        Department updatedDepartment = departmentRepository.save(department);
        log.info("Department updated successfully: {}", updatedDepartment.getCode());

        return departmentMapper.toResponse(updatedDepartment);
    }

    @Override
    @CacheEvict(value = "departments", key = "#id")
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Attempting to delete department id: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Check if department is protected
        if (PROTECTED_DEPARTMENTS.contains(department.getCode().toUpperCase())) {
            throw new BusinessRuleException("Cannot delete protected department: " + department.getCode());
        }

        // Check if department has employees
        if (departmentRepository.hasEmployees(id)) {
            throw new BusinessRuleException("Cannot delete department that has employees. Please reassign employees first.");
        }

        departmentRepository.delete(department);
        log.info("Department deleted successfully: {}", department.getCode());
    }

    @Override
    @CacheEvict(value = "departments", key = "#id")
    @Transactional
    public DepartmentResponse updateDepartmentStatus(Long id, UpdateDepartmentStatusRequest request) {
        log.info("Updating department status id: {}, isActive: {}", id, request.getIsActive());

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        department.setActive(request.getIsActive());

        Department updatedDepartment = departmentRepository.save(department);
        log.info("Department status updated to {} for: {}",
                request.getIsActive() ? "active" : "inactive",
                updatedDepartment.getCode());

        return departmentMapper.toResponse(updatedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCodeUnique(String code, Long excludeId) {
        if (excludeId == null) {
            return !departmentRepository.existsByCode(code.toUpperCase());
        }
        return !departmentRepository.existsByCodeAndIdNot(code.toUpperCase(), excludeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEmployees(Long departmentId) {
        return departmentRepository.hasEmployees(departmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateDepartmentDeletion(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        if (PROTECTED_DEPARTMENTS.contains(department.getCode().toUpperCase())) {
            throw new BusinessRuleException("Cannot delete protected department: " + department.getCode());
        }

        if (hasEmployees(departmentId)) {
            throw new BusinessRuleException("Department has employees. Please reassign them before deletion.");
        }
    }

    @Override
    @Cacheable(value = "activeDepartments")
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllActiveDepartments() {
        log.debug("Fetching all active departments");

        List<Department> departments = departmentRepository.findByIsActiveTrue();
        return departmentMapper.toResponseList(departments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentsByManager(Long managerId) {
        log.debug("Fetching departments by manager id: {}", managerId);

        List<Department> departments = departmentRepository.findByManagerId(managerId);
        return departmentMapper.toResponseList(departments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getDepartmentStatistics() {
        log.debug("Fetching department statistics");

        List<Object[]> results = departmentRepository.findDepartmentWithEmployeeCount();

        return results.stream()
                .map(result -> {
                    Department department = (Department) result[0];
                    Long employeeCount = (Long) result[1];

                    DepartmentDTO dto = departmentMapper.toDTO(department);
                    dto.setEmployeeCount(employeeCount.intValue());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDetailResponse> getPotentialManagers(Long departmentId) {
//        log.debug("Fetching potential managers for department: {}", departmentId);
//
//        // Get all active users with managerial roles or appropriate position
//        List<User> potentialManagers = userRepository.findPotentialDepartmentManagers();
//
//        return potentialManagers.stream()
//                .map(user -> UserDTO.builder()
//                        .id(user.getId())
//                        .name(user.getName())
//                        .email(user.getEmail())
//                        .position(user.getPosition())
//                        .build())
//                .collect(Collectors.toList());
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartmentEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    @Override
    @Transactional
    public void updateDepartmentManager(Long departmentId, Long managerId) {
        log.info("Updating manager for department: {} to user: {}", departmentId, managerId);

        Department department = getDepartmentEntity(departmentId);
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId));

        if (!manager.isActive()) {
            throw new ValidationException("Cannot assign inactive user as department manager");
        }

        department.setManager(manager);
        departmentRepository.save(department);

        log.info("Manager updated successfully for department: {}", department.getCode());
    }

    @Override
    @Transactional
    public void removeDepartmentManager(Long departmentId) {
        log.info("Removing manager from department: {}", departmentId);

        Department department = getDepartmentEntity(departmentId);
        department.setManager(null);
        departmentRepository.save(department);

        log.info("Manager removed successfully from department: {}", department.getCode());
    }
}
