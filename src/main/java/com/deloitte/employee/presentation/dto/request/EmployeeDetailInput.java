package com.deloitte.employee.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
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
}
