package com.deloitte.employee.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
// This will also be the response for web api(s)
public class EmployeeDetail {

    private String id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String dob;
    private Boolean isActive;
    private String designation;
    private String managerId;
    private String address;
    private String startedAt;

}
