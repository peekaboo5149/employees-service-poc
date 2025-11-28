package com.deloitte.employee.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "GenericEmployeeListResponse",
        description = "Paginated list of employees"
)
public class EmployeeListResponse extends GenericListResponse<EmployeeDetail> {
}
