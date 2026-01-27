package com.gcompany.employeemanagement.repository;

import com.gcompany.employeemanagement.enums.RoleType;
import com.gcompany.employeemanagement.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    Optional<Role> findByCode(String code);

    List<Role> findByType(RoleType type);

    List<Role> findBySystem(boolean system);

    List<Role> findByDefaultRole(boolean defaultRole);

    List<Role> findByActive(boolean active);



    @Query("SELECT r FROM Role r WHERE r.parentRole.id = :parentId")
    List<Role> findByParentRoleId(@Param("parentId") Long parentId);

    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Role r WHERE r.code IN :codes")
    List<Role> findByCodes(@Param("codes") List<String> codes);

    boolean existsByName(String name);

    boolean existsByCode(String code);

}
