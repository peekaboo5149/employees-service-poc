package com.deloitte.employee.presentation.service;

import com.deloitte.employee.domain.enums.EmployeeSortField;
import com.deloitte.employee.domain.mapper.ExceptionMapper;
import com.deloitte.employee.domain.repository.IEmployeeManagementDao;
import com.deloitte.employee.domain.valueobject.Query;
import com.deloitte.employee.presentation.dto.request.EmployeeDetailInput;
import com.deloitte.employee.presentation.dto.request.EmployeeQueryRequest;
import com.deloitte.employee.presentation.dto.response.EmployeeDetail;
import com.deloitte.employee.presentation.exception.AppException;
import com.deloitte.employee.presentation.exception.ErrorCode;
import com.deloitte.employee.domain.entities.ErrorDetail;
import com.deloitte.employee.presentation.exception.ErrorResponse;
import com.deloitte.employee.presentation.mapper.EmployeeDataMapper;
import com.deloitte.employee.presentation.mapper.QueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeManagementService {

    private final IEmployeeManagementDao employeeRepository;
    private final EmployeeDataMapper employeeDataMapper;
    private final ExceptionMapper<AppException> exceptionMapper;
    private final QueryMapper queryMapper;


    public EmployeeDetail getEmployeeById(String id) {
        return employeeRepository.getEmployeeById(id).fold(
                f -> {
                    throw exceptionMapper.map(f);
                },
                opt -> opt.map(employeeDataMapper::toDetail)
                        .getOrElseThrow(() -> AppException.of(
                                        ErrorResponse.builder()
                                                .code(HttpStatus.NOT_FOUND.value())
                                                .errorCode(ErrorCode.RESOURCE_NOT_FOUND)
                                                .message("User not found")
                                                .errorDetails(List.of(
                                                        ErrorDetail.builder()
                                                                .code("ERR_USER_NOT_FOUND")
                                                                .field("id")
                                                                .message("No user exists with the provided ID or email.")
                                                                .build()
                                                ))
                                                .build()
                                )
                        )
        );
    }

    public List<EmployeeDetail> getAllEmployee(EmployeeQueryRequest query) {
        return employeeRepository.getEmployees(queryMapper.transform(query, exceptionMapper))
                .fold(
                        exceptionMapper::mapAndThrow,
                        list -> list.stream()
                                .map(employeeDataMapper::toDetail)
                                .toList()
                );
    }


    public EmployeeDetail createEmployee(EmployeeDetailInput employee) {
        return employeeRepository.createEmployee(employeeDataMapper.toEntity(employee))
                .fold(
                        exceptionMapper::mapAndThrow,
                        employeeDataMapper::toDetail
                );
    }

    public EmployeeDetail updateEmployee(String id, EmployeeDetailInput employee) {
        return employeeRepository.updateEmployee(id, employeeDataMapper.toEntity(employee))
                .fold(
                        exceptionMapper::mapAndThrow,
                        employeeDataMapper::toDetail
                );
    }


    public void deleteEmployee(String id) {
        employeeRepository.deleteEmployee(id)
                .peek(exceptionMapper::mapAndThrow);
    }
}
