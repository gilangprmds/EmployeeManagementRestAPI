package com.gcompany.employeemanagement.repository;

import com.gcompany.employeemanagement.model.Attendance;
import com.gcompany.employeemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByUser_IdAndDateAndCheckoutTimeIsNull(Long userId, LocalDate date);

    Optional<Attendance> findFirstByUser_IdAndDateAndCheckoutTimeIsNull(Long userId, LocalDate date);

    Optional<Attendance> findFirstByUser_IdAndDate(Long userId, LocalDate date);

    List<Attendance> findByUser_IdOrderByDateDesc(Long userId);
}
