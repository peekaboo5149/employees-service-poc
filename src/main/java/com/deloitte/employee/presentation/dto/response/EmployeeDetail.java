package com.deloitte.employee.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(name = "EmployeeDetail", description = "Employee details returned in API responses")
public class EmployeeDetail {

    @Schema(example = "7da9676e-a38e-4556-a633-96fc097c6151",
            description = "Unique ID of the employee")
    private String id;

    @Schema(example = "jack.davis@example.com",
            description = "Employee email address")
    private String email;

    @Schema(example = "Jack Davis",
            description = "Full legal name of the employee")
    private String fullName;

    @Schema(example = "+1-510-555-1006",
            description = "Employee contact phone number")
    private String phoneNumber;

    @Schema(example = "1991-07-19",
            description = "Date of birth of the employee (YYYY-MM-DD)")
    private String dob;

    @Schema(example = "true",
            description = "Whether the employee is active")
    private Boolean isActive;

    @Schema(example = "DevOps Engineer",
            description = "Job designation or title of the employee")
    private String designation;

    @Schema(example = "mgr-1005",
            description = "ID of the manager assigned to the employee. Nullable")
    private String managerId;

    @Schema(example = "301 Sunset Blvd, Oakland, CA",
            description = "Residential address of the employee")
    private String address;

    @Schema(example = "2023-01-15T09:30:00",
            description = "Timestamp when the employee started (ISO-8601)")
    private String startedAt;
}
