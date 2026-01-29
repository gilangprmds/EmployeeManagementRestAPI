package com.gcompany.employeemanagement.repository;

import com.gcompany.employeemanagement.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // Find by department code
    Optional<Department> findByCode(String code);

    // Check if code exists (excluding current department)
    boolean existsByCodeAndIdNot(String code, Long id);

    // Check if code exists
    boolean existsByCode(String code);

    // Find all active departments
    List<Department> findByIsActiveTrue();

    // Find all active departments with pagination
    Page<Department> findByIsActiveTrue(Pageable pageable);

    // Search departments by name or code (case-insensitive)
    @Query("SELECT d FROM Department d WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:activeOnly IS FALSE OR d.isActive = :activeOnly)")
    Page<Department> searchDepartments(
            @Param("search") String search,
            @Param("activeOnly") boolean activeOnly,
            Pageable pageable);

    // Count active departments
    long countByIsActiveTrue();

    // Find departments by manager
    List<Department> findByManagerId(Long managerId);

    // Find departments where manager is null
    List<Department> findByManagerIsNull();

    // Find all departments ordered by name
    List<Department> findAllByOrderByNameAsc();

    // Custom query for dashboard statistics
    @Query("SELECT d, COUNT(u) as employeeCount FROM Department d " +
            "LEFT JOIN d.employees u " +
            "WHERE d.isActive = true " +
            "GROUP BY d " +
            "ORDER BY employeeCount DESC")
    List<Object[]> findDepartmentWithEmployeeCount();

    // Check if department has employees
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM Department d JOIN d.employees u WHERE d.id = :departmentId")
    boolean hasEmployees(@Param("departmentId") Long departmentId);
}
