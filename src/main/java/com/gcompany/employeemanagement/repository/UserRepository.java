package com.gcompany.employeemanagement.repository;

import com.gcompany.employeemanagement.enums.Role;
import com.gcompany.employeemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<Object> findByRole(Role role);
}
