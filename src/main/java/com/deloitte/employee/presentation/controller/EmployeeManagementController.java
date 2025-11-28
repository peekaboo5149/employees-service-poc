package com.deloitte.employee.presentation.controller;

import com.deloitte.employee.presentation.dto.request.EmployeeDetailInput;
import com.deloitte.employee.presentation.dto.request.QueryRequest;
import com.deloitte.employee.presentation.dto.response.EmployeeDetail;
import com.deloitte.employee.presentation.dto.response.EmployeeListResponse;
import com.deloitte.employee.presentation.helper.validation.CreateGroup;
import com.deloitte.employee.presentation.helper.validation.UpdateGroup;
import com.deloitte.employee.presentation.service.IEmployeeManagementService;
import com.deloitte.employee.presentation.service.ResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.deloitte.employee.presentation.helper.Constants.EMPLOYEE_LIST_QUERY_IMPL;

@Tag(name = "Employee Management", description = "CRUD operations for employees")
@RestController
@RequestMapping("/employees")
class EmployeeManagementController {
    private final IEmployeeManagementService employeeManagementService;
    private final ResponseMapper<EmployeeDetail> responseMapper;

    public EmployeeManagementController(IEmployeeManagementService employeeManagementService,
                                        @Qualifier(EMPLOYEE_LIST_QUERY_IMPL) ResponseMapper<EmployeeDetail> responseMapper) {
        this.employeeManagementService = employeeManagementService;
        this.responseMapper = responseMapper;
    }

    @Operation(
            summary = "Get employee by ID",
            description = "Fetches a single employee based on the provided ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Employee details found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeDetail.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Employee not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "UserNotFound",
                                            description = "No employee exists with the provided ID",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "errorCode": "ERR_101: Resource not found",
                                                      "errorDetails": [
                                                        {
                                                          "code": "ERR_USER_NOT_FOUND",
                                                          "field": "id",
                                                          "message": "No user exists with the provided ID or email."
                                                        }
                                                      ],
                                                      "message": "User not found"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Service temporarily unavailable",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "ServiceUnavailableError",
                                            description = "Internal service or database failure",
                                            value = """
                                                    {
                                                      "code": 500,
                                                      "errorCode": "ERR_105: Service unavailable",
                                                      "errorDetails": [
                                                        {
                                                          "code": "UNREACHABLE",
                                                          "field": "db",
                                                          "message": "Service not reachable"
                                                        }
                                                      ],
                                                      "message": "Service temporarily unavailable"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable String id) {
        return ResponseEntity.ok(employeeManagementService.getEmployeeById(id));
    }

    @Operation(
            summary = "Get paginated list of employees",
            description = "Supports sorting, searching, and pagination",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list of employees",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeListResponse.class),
                                    examples = @ExampleObject(
                                            name = "EmployeeListExample",
                                            description = "Example paginated response",
                                            value = """
                                                    {
                                                      "data": [
                                                        {
                                                          "id": "7da9676e-a38e-4556-a633-96fc097c6151",
                                                          "email": "jack.davis@example.com",
                                                          "fullName": "Jack Davis",
                                                          "phoneNumber": "+1-510-555-1006",
                                                          "dob": "1991-07-19",
                                                          "isActive": true,
                                                          "designation": "DevOps Engineer",
                                                          "managerId": "mgr-1005",
                                                          "address": "301 Sunset Blvd, Oakland, CA",
                                                          "startedAt": "2023-01-15T09:30:00"
                                                        }
                                                      ],
                                                      "meta": {
                                                        "page": 0,
                                                        "size": 20,
                                                        "hasNext": true
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Service temporarily unavailable",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "ServiceUnavailableError",
                                            description = "Internal service failure",
                                            value = """
                                                    {
                                                      "code": 500,
                                                      "errorCode": "ERR_105: Service unavailable",
                                                      "errorDetails": [
                                                        {
                                                          "code": "UNREACHABLE",
                                                          "field": "db",
                                                          "message": "Service not reachable"
                                                        }
                                                      ],
                                                      "message": "Service temporarily unavailable"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/all")
    public ResponseEntity<?> getAll(@RequestBody QueryRequest req) {
        final QueryRequest scaled = responseMapper.scaled(req);
        return ResponseEntity.ok(responseMapper
                .toResponse(req,scaled, employeeManagementService.getAllEmployee(scaled)));
    }

    @Operation(
            summary = "Create a new employee",
            description = "All required fields must be provided for creation",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Employee created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeDetail.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Employee already exists",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "EmailExistsError",
                                            description = "Duplicate email conflict",
                                            value = """
                                                    {
                                                      "code": 409,
                                                      "errorCode": "ERR_103: Resource conflict",
                                                      "errorDetails": [
                                                        {
                                                          "code": "ERR_EMAIL_EXISTS",
                                                          "field": "email",
                                                          "message": "Email already exists"
                                                        }
                                                      ],
                                                      "message": "Resource conflict failure occurred."
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "ValidationError",
                                            description = "Validation failed for incoming request",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "errorCode": "ERR_102: Bad request",
                                                      "errorDetails": [
                                                        {
                                                          "code": "VALIDATION_ERROR",
                                                          "field": "fullName",
                                                          "message": "must not be blank"
                                                        }
                                                      ],
                                                      "message": "Validation failed for request"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Service temporarily unavailable",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "ServiceUnavailableError",
                                            description = "Internal or downstream service failure",
                                            value = """
                                                    {
                                                      "code": 500,
                                                      "errorCode": "ERR_105: Service unavailable",
                                                      "errorDetails": [
                                                        {
                                                          "code": "UNREACHABLE",
                                                          "field": "db",
                                                          "message": "Service not reachable"
                                                        }
                                                      ],
                                                      "message": "Service temporarily unavailable"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/create")
    public ResponseEntity<?> create(
            @Validated(CreateGroup.class)
            @RequestBody EmployeeDetailInput requestBody) {
        return ResponseEntity.ok(employeeManagementService.createEmployee(requestBody));
    }


    @Operation(
            summary = "Partially update an employee",
            description = "PATCH semantics — only provided fields are updated",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Employee updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EmployeeDetail.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Employee not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "UserNotFound",
                                            description = "No employee exists with the provided ID",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "errorCode": "ERR_101: Resource not found",
                                                      "errorDetails": [
                                                        {
                                                          "code": "ERR_USER_NOT_FOUND",
                                                          "field": "id",
                                                          "message": "No user exists with the provided ID or email."
                                                        }
                                                      ],
                                                      "message": "User not found"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "ValidationError",
                                            description = "Invalid fields provided in the update request",
                                            value = """
                                                    {
                                                      "code": 400,
                                                      "errorCode": "ERR_102: Bad request",
                                                      "errorDetails": [
                                                        {
                                                          "code": "VALIDATION_ERROR",
                                                          "field": "email",
                                                          "message": "must be a valid email format"
                                                        }
                                                      ],
                                                      "message": "Validation failed for request"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Service temporarily unavailable",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "ServiceUnavailableError",
                                            description = "Internal service or database failure",
                                            value = """
                                                    {
                                                      "code": 500,
                                                      "errorCode": "ERR_105: Service unavailable",
                                                      "errorDetails": [
                                                        {
                                                          "code": "UNREACHABLE",
                                                          "field": "db",
                                                          "message": "Service not reachable"
                                                        }
                                                      ],
                                                      "message": "Service temporarily unavailable"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(description = "ID of employee to update") @PathVariable String id,
            @Validated(UpdateGroup.class)
            @RequestBody EmployeeDetailInput requestBody) {
        return ResponseEntity.ok(employeeManagementService.updateEmployee(id, requestBody));
    }

    @Operation(
            summary = "Delete an employee",
            description = "Removes employee by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Employee deleted successfully",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Employee not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "UserNotFound",
                                            description = "No employee exists with the provided ID",
                                            value = """
                                                    {
                                                      "code": 404,
                                                      "errorCode": "ERR_101: Resource not found",
                                                      "errorDetails": [
                                                        {
                                                          "code": "ERR_USER_NOT_FOUND",
                                                          "field": "id",
                                                          "message": "No user exists with the provided ID or email."
                                                        }
                                                      ],
                                                      "message": "User not found"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Service temporarily unavailable",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "ServiceUnavailableError",
                                            description = "Internal service/database connectivity failure",
                                            value = """
                                                    {
                                                      "code": 500,
                                                      "errorCode": "ERR_105: Service unavailable",
                                                      "errorDetails": [
                                                        {
                                                          "code": "UNREACHABLE",
                                                          "field": "db",
                                                          "message": "Service not reachable"
                                                        }
                                                      ],
                                                      "message": "Service temporarily unavailable"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID of employee to delete") @PathVariable String id) {
        employeeManagementService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }

}
