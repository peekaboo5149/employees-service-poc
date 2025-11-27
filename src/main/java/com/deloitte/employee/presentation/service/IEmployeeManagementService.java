package com.deloitte.employee.presentation.service;

import com.deloitte.employee.presentation.dto.request.EmployeeDetailInput;
import com.deloitte.employee.presentation.dto.request.QueryRequest;
import com.deloitte.employee.presentation.dto.response.EmployeeDetail;

import java.util.List;

public interface IEmployeeManagementService {
    EmployeeDetail getEmployeeById(String id);

    List<EmployeeDetail> getAllEmployee(QueryRequest query);

    EmployeeDetail createEmployee(EmployeeDetailInput employee);

    EmployeeDetail updateEmployee(String id, EmployeeDetailInput employee);

    void deleteEmployee(String id);
}
