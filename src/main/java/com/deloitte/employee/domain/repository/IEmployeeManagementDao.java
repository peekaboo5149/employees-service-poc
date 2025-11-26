package com.deloitte.employee.domain.repository;

import com.deloitte.employee.domain.enums.EmployeeSortField;
import com.deloitte.employee.domain.failure.OperationFailure;
import com.deloitte.employee.domain.entities.Employee;
import com.deloitte.employee.domain.valueobject.Query;
import io.vavr.control.Either;
import io.vavr.control.Option;

import java.util.List;

public interface IEmployeeManagementDao {

    Either<OperationFailure, List<Employee>> getEmployees(Query<EmployeeSortField> query);

    Either<OperationFailure, Option<Employee>> getEmployeeById(String id);

    Either<OperationFailure, Employee> createEmployee(Employee employee);

    Either<OperationFailure, Employee> updateEmployee(String id, Employee employee);

    Option<OperationFailure> deleteEmployee(String id);
}
