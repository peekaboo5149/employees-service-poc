package com.deloitte.employee.presentation.dto.request;


import com.deloitte.employee.presentation.helper.validation.CreateGroup;
import com.deloitte.employee.presentation.helper.validation.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EmployeeDetailInput {

    @NotBlank(groups = CreateGroup.class)
    @Email(groups = {CreateGroup.class, UpdateGroup.class})
    private String email;

    @NotBlank(groups = CreateGroup.class)
    @Size(min = 3, max = 50, groups = {CreateGroup.class, UpdateGroup.class})
    private String fullName;

    @NotBlank(groups = CreateGroup.class)
    @Size(min = 6, message = "Password must be at least 6 characters", groups = CreateGroup.class)
    private String password;

    @NotBlank(groups = CreateGroup.class)
    private String phoneNumber;

    private LocalDate dob;

    private Boolean isActive;

    @NotBlank(groups = CreateGroup.class)
    private String designation;

    private String managerId;

    private String address;
}
