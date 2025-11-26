package com.deloitte.employee.presentation.dto.request;

import lombok.Builder;

@Builder
public class EmployeeDetailInput {

    private String id;
    private String email;
    private String fullName;
    private String password;

}
