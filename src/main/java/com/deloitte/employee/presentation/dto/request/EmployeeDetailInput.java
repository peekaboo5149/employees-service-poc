package com.deloitte.employee.presentation.dto.request;


import com.deloitte.employee.presentation.helper.validation.CreateGroup;
import com.deloitte.employee.presentation.helper.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(name = "EmployeeDetailInput", description = "Input payload for employee creation/update")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EmployeeDetailInput {

    @Schema(example = "jack.davis@example.com")
    @NotBlank(groups = CreateGroup.class)
    @Email(groups = {CreateGroup.class, UpdateGroup.class})
    private String email;

    @Schema(example = "Jack Davis")
    @NotBlank(groups = CreateGroup.class)
    @Size(min = 3, max = 50, groups = {CreateGroup.class, UpdateGroup.class})
    private String fullName;

    @Schema(example = "StrongPass#1")
    @NotBlank(groups = CreateGroup.class)
    @Size(min = 6, message = "Password must be at least 6 characters", groups = CreateGroup.class)
    private String password;

    @Schema(example = "+1-510-555-1006")
    @NotBlank(groups = CreateGroup.class)
    private String phoneNumber;

    @Schema(example = "1991-07-19")
    private LocalDate dob;

    @Schema(example = "true")
    private Boolean isActive;

    @Schema(example = "DevOps Engineer")
    @NotBlank(groups = CreateGroup.class)
    private String designation;

    @Schema(example = "qkjabdjhad-q2ewedsdc-qewdscsd")
    private String managerId;

    @Schema(example = "301 Sunset Blvd, Oakland, CA")
    private String address;
}
