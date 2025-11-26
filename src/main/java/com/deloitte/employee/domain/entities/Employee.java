package com.deloitte.employee.domain.entities;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Employee {
    private String id;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private LocalDate dob;
    private boolean isActive;
    private String designation;
    private String managerId;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
