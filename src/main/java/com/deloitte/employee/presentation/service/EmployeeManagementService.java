package com.deloitte.employee.presentation.service;

import com.deloitte.employee.domain.mapper.ExceptionMapper;
import com.deloitte.employee.domain.repository.IEmployeeRepository;
import com.deloitte.employee.presentation.dto.request.EmployeeDetailInput;
import com.deloitte.employee.presentation.dto.response.EmployeeDetail;
import com.deloitte.employee.presentation.exception.AppException;
import com.deloitte.employee.presentation.exception.ErrorCode;
import com.deloitte.employee.presentation.exception.ErrorDetail;
import com.deloitte.employee.presentation.exception.ErrorResponse;
import com.deloitte.employee.presentation.mapper.EmployeeDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeManagementService {

    private final IEmployeeRepository employeeRepository;
    private final EmployeeDataMapper employeeDataMapper;
    private final ExceptionMapper<AppException> exceptionMapper;


    public EmployeeDetail getEmployeeById(String id) {
        return employeeRepository.getEmployeeById(id).fold(
                f -> {
                    throw exceptionMapper.map(f);
                },
                opt -> opt.map(employeeDataMapper::toDetail)
                        .getOrElseThrow(() -> AppException.builder()
                                .errorDetail(ErrorResponse.builder()
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
                                        .build())
                                .cause(null)
                                .build())
        );
    }

    public List<EmployeeDetail> getAllEmployee() {
        return employeeRepository.getEmployees()
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

    public EmployeeDetail updateEmployee(EmployeeDetailInput employee) {
        return employeeRepository.updateEmployee(employeeDataMapper.toEntity(employee))
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
