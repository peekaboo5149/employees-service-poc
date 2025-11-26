package com.deloitte.employee.domain.repository;

import com.deloitte.employee.domain.failure.OperationFailure;
import com.deloitte.employee.domain.entities.Employee;
import io.vavr.control.Either;
import io.vavr.control.Option;

import java.util.List;

public interface IEmployeeRepository {

    Either<OperationFailure, List<Employee>> getEmployees();

    Either<OperationFailure, Option<Employee>> getEmployeeById(String id);

    Either<OperationFailure, Employee> createEmployee(Employee employee);

    Either<OperationFailure, Employee> updateEmployee(Employee employee);

    Option<OperationFailure> deleteEmployee(String id);
}
