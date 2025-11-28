package com.deloitte.employee.infra.mapper;

import com.deloitte.employee.domain.entities.Employee;
import com.deloitte.employee.domain.entities.ErrorDetail;
import com.deloitte.employee.domain.failure.ValidationFailure;
import com.deloitte.employee.infra.entities.EmployeeJPAEntity;
import com.deloitte.employee.infra.repositories.EmployeeJPARepository;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
                .isActive(employee.getIsActive())
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

    public Either<ValidationFailure, EmployeeJPAEntity> merge(EmployeeJPAEntity employeeEntity, Employee employee) {
        if (employeeEntity == null || employee == null) {
            return Either.right(employeeEntity);
        }

        // ============ PATCH SIMPLE FIELDS ============
        if (employee.getEmail() != null) employeeEntity.setEmail(employee.getEmail());
        if (employee.getPassword() != null) employeeEntity.setPassword(employee.getPassword());
        if (employee.getFullName() != null) employeeEntity.setFullName(employee.getFullName());
        if (employee.getPhoneNumber() != null) employeeEntity.setPhoneNumber(employee.getPhoneNumber());
        if (employee.getDob() != null) employeeEntity.setDob(employee.getDob());
        if (employee.getDesignation() != null) employeeEntity.setDesignation(employee.getDesignation());
        if (employee.getAddress() != null) employeeEntity.setAddress(employee.getAddress());
        if (employee.getUpdatedBy() != null) employeeEntity.setUpdatedBy(employee.getUpdatedBy());
        if (employee.getUpdatedAt() != null) employeeEntity.setUpdatedAt(employee.getUpdatedAt());
        if (employee.getIsActive() != null) employeeEntity.setActive(employee.getIsActive());

        // ============ MANAGER LOGIC ============

        String incomingManagerId = employee.getManagerId();

        // CASE 1: No managerId provided → do nothing
        if (incomingManagerId == null) {
            return Either.right(employeeEntity);
        }

        // CASE 2: explicit removal: "NULL" (string literal)
        if ("NULL".equalsIgnoreCase(incomingManagerId)) {
            employeeEntity.setManager(null);
            return Either.right(employeeEntity);
        }

        // CASE 3: Prevent self-manager
        if (employeeEntity.getId().equals(incomingManagerId)) {
            return Either.left(new ValidationFailure(
                    List.of(ErrorDetail.builder()
                            .field("managerId")
                            .message("Employee cannot manage himself")
                            .code("ERR_SELF_MANAGER")
                            .build())
            ));
        }

        // CASE 4: Validate manager existence
        EmployeeJPAEntity managerEntity =
                employeeJPARepository.findById(incomingManagerId).orElse(null);

        if (managerEntity == null) {
            return Either.left(new ValidationFailure(
                    List.of(ErrorDetail.builder()
                            .field("managerId")
                            .message("Manager not found")
                            .code("ERR_MANAGER_NOT_FOUND")
                            .build())
            ));
        }

        // CASE 5: Set new manager
        employeeEntity.setManager(managerEntity);

        return Either.right(employeeEntity);
    }


}
