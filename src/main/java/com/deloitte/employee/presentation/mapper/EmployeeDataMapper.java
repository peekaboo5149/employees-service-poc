package com.deloitte.employee.presentation.mapper;

import com.deloitte.employee.domain.entities.Employee;
import com.deloitte.employee.presentation.dto.request.EmployeeDetailInput;
import com.deloitte.employee.presentation.dto.response.EmployeeDetail;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmployeeDataMapper {
    public EmployeeDetail toDetail(Employee employee) {
        return EmployeeDetail.builder()
                .id(employee.getId())
                .email(employee.getEmail())
                .fullName(employee.getFullName())
                .phoneNumber(employee.getPhoneNumber())
                .dob(employee.getDob().toString()) // TODO: Use some formatter
                .address(employee.getAddress())
                .designation(employee.getDesignation())
                .isActive(employee.isActive())
                .startedAt(employee.getCreatedAt().toString()) // TODO: Use some formatter
                .managerId(employee.getManagerId())
                .build();
    }

    public Employee toEntity(EmployeeDetailInput employee) {
        return Employee.builder()
                .id(UUID.randomUUID().toString())
                .email(employee.getEmail())
                .password(employee.getPassword())
                .isActive(true)
                .address(employee.getAddress())
                .managerId(null)
                .dob(employee.getDob())
                .fullName(employee.getFullName())
                .phoneNumber(employee.getPhoneNumber())
                .designation(employee.getDesignation())
                .build();
    }
}
