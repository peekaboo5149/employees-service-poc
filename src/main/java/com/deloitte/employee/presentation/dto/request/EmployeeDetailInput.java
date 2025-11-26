package com.deloitte.employee.presentation.dto.request;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public class EmployeeDetailInput {

    private String email;
    private String fullName;
    private String password;
    private String phoneNumber;
    private LocalDate dob;
    private Boolean isActive;
    private String designation;
    private String managerId;
    private String address;
    private String startedAt;
}
