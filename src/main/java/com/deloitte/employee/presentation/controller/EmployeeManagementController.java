package com.deloitte.employee.presentation.controller;

import com.deloitte.employee.presentation.dto.request.EmployeeDetailInput;
import com.deloitte.employee.presentation.dto.request.QueryRequest;
import com.deloitte.employee.presentation.dto.response.EmployeeDetail;
import com.deloitte.employee.presentation.service.IEmployeeManagementService;
import com.deloitte.employee.presentation.service.ResponseMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.deloitte.employee.presentation.helper.Constants.EMPLOYEE_LIST_QUERY_IMPL;

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

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable String id) {
        return ResponseEntity.ok(employeeManagementService.getEmployeeById(id));
    }

    @PostMapping("/all")
    public ResponseEntity<?> getAll(@RequestBody QueryRequest req) {
        final QueryRequest scaled = responseMapper.scaled(req);
        return ResponseEntity.ok(responseMapper
                .toResponse(req,scaled, employeeManagementService.getAllEmployee(scaled)));
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody EmployeeDetailInput requestBody) {
        return ResponseEntity.ok(employeeManagementService.createEmployee(requestBody));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody EmployeeDetailInput requestBody) {
        return ResponseEntity.ok(employeeManagementService.updateEmployee(id, requestBody));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        employeeManagementService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }
}
