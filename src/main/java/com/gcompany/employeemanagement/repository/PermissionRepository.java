package com.gcompany.employeemanagement.repository;

import com.gcompany.employeemanagement.enums.ActionType;
import com.gcompany.employeemanagement.enums.ResourceType;
import com.gcompany.employeemanagement.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    List<Permission> findByResource(ResourceType resource);

    List<Permission> findByAction(ActionType action);

    List<Permission> findByResourceAndAction(ResourceType resource, ActionType action);

    List<Permission> findByCategory(String category);

    List<Permission> findByActive(boolean active);

    @Query("SELECT p FROM Permission p WHERE p.name IN :names")
    List<Permission> findAllByNameIn(@Param("names") List<String> names);

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.scope = :scope")
    Optional<Permission> findByResourceAndActionAndScope(
            @Param("resource") ResourceType resource,
            @Param("action") ActionType action,
            @Param("scope") String scope);

    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.code = :roleCode")
    List<Permission> findByRoleCode(@Param("roleCode") String roleCode);

    boolean existsByName(String name);

    boolean existsByResourceAndAction(ResourceType resource, ActionType action);
}
