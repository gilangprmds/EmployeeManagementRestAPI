package com.gcompany.employeemanagement.repository;

import com.gcompany.employeemanagement.enums.UserStatus;
import com.gcompany.employeemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // Basic queries
    Optional<User> findByEmail(String email);

    List<User> findByFullNameContainingIgnoreCase(String fullName);

    // Status-based queries (menggunakan enum)
    List<User> findByStatus(UserStatus status);

    // Combined status queries
    @Query("SELECT u FROM User u WHERE u.status IN :statuses")
    List<User> findByStatuses(@Param("statuses") List<UserStatus> statuses);

    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findAllActiveUsers();

    @Query("SELECT u FROM User u WHERE u.status IN ('LOCKED', 'SUSPENDED')")
    List<User> findAllBlockedUsers();

    // Role-based queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.code = :roleCode")
    List<User> findByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.code = :roleCode AND u.status = :status")
    List<User> findByRoleCodeAndStatus(
            @Param("roleCode") String roleCode,
            @Param("status") UserStatus status);

    boolean existsByEmail(String email);

    boolean existsByEmailAndStatus(String email, UserStatus status);

    // Check if user has specific role
    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.roles r WHERE u.id = :userId AND r.code = :roleCode")
    boolean userHasRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);

}
