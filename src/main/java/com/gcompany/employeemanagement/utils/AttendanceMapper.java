package com.gcompany.employeemanagement.utils;

import com.gcompany.employeemanagement.dto.resp.AttendanceResponse;
import com.gcompany.employeemanagement.model.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    @Mapping(source = "user.profilePicture", target = "userProfileImageUrl")
    @Mapping(source = "user.email", target = "userEmail")
//    @Mapping(source = "user.role", target = "userRole")
    AttendanceResponse toDTO(Attendance attendance);
}
