package com.deloitte.employee.infra.mapper;

import com.deloitte.employee.domain.entities.Employee;
import com.deloitte.employee.infra.entities.EmployeeJPAEntity;
import com.deloitte.employee.infra.repositories.EmployeeJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmployeeJPAMapper {

    private final EmployeeJPARepository employeeJPARepository;

    public EmployeeJPAEntity toEntity(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeJPAEntity managerEntity = null;
        if (employee.getManagerId() != null) {
            managerEntity = employeeJPARepository.findById(employee.getManagerId()).orElse(null);
        }

        return EmployeeJPAEntity.builder()
                .id(employee.getId())
                .email(employee.getEmail())
                .password(employee.getPassword())
                .fullName(employee.getFullName())
                .phoneNumber(employee.getPhoneNumber())
                .dob(employee.getDob())
                .isActive(employee.isActive())
                .designation(employee.getDesignation())
                .manager(managerEntity)
                .address(employee.getAddress())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .createdBy(employee.getCreatedBy())
                .updatedBy(employee.getUpdatedBy())
                .build();
    }

    public Employee toDomain(EmployeeJPAEntity entity) {
        if (entity == null) {
            return null;
        }

        return Employee.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .fullName(entity.getFullName())
                .phoneNumber(entity.getPhoneNumber())
                .dob(entity.getDob())
                .isActive(entity.isActive())
                .designation(entity.getDesignation())
                .managerId(entity.getManager() != null ? entity.getManager().getId() : null)
                .address(entity.getAddress())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public EmployeeJPAEntity merge(EmployeeJPAEntity employeeEntity, Employee employee) {
        // leave the id
        if (employeeEntity == null || employee == null) {
            return employeeEntity;
        }

        employeeEntity.setEmail(employee.getEmail());
        employeeEntity.setPassword(employee.getPassword());
        employeeEntity.setFullName(employee.getFullName());
        employeeEntity.setPhoneNumber(employee.getPhoneNumber());
        employeeEntity.setDob(employee.getDob());
        employeeEntity.setActive(employee.isActive());
        employeeEntity.setDesignation(employee.getDesignation());
        employeeEntity.setAddress(employee.getAddress());
        employeeEntity.setUpdatedBy(employee.getUpdatedBy());
        employeeEntity.setUpdatedAt(employee.getUpdatedAt());

        // Handle manager relationship
        if (employee.getManagerId() != null) {
            EmployeeJPAEntity managerEntity = employeeJPARepository.findById(employee.getManagerId()).orElse(null);
            employeeEntity.setManager(managerEntity);
        } else {
            employeeEntity.setManager(null);
        }

        return employeeEntity;

    }
}
