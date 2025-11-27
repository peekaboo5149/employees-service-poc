package com.deloitte.employee.presentation.service.impl;

import com.deloitte.employee.domain.entities.Employee;
import com.deloitte.employee.domain.entities.ErrorDetail;
import com.deloitte.employee.domain.enums.EmployeeSortField;
import com.deloitte.employee.domain.failure.OperationFailure;
import com.deloitte.employee.domain.failure.SystemFailure;
import com.deloitte.employee.domain.mapper.ExceptionMapper;
import com.deloitte.employee.domain.repository.IEmployeeManagementDao;
import com.deloitte.employee.domain.valueobject.Query;
import com.deloitte.employee.presentation.dto.request.EmployeeDetailInput;
import com.deloitte.employee.presentation.dto.request.QueryRequest;
import com.deloitte.employee.presentation.dto.response.EmployeeDetail;
import com.deloitte.employee.presentation.exception.AppException;
import com.deloitte.employee.presentation.exception.ErrorCode;
import com.deloitte.employee.presentation.exception.ErrorResponse;
import com.deloitte.employee.presentation.mapper.EmployeeDataMapper;
import com.deloitte.employee.presentation.mapper.QueryMapper;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.deloitte.employee.helper.TestUtils.mockExceptionMapper;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EmployeeManagementServiceTest {

    private IEmployeeManagementDao employeeRepository;
    private EmployeeDataMapper employeeDataMapper;
    private ExceptionMapper<AppException> exceptionMapper;
    private QueryMapper queryMapper;
    private EmployeeManagementService sut;

    @BeforeEach
    void setup() {
        employeeRepository = mock(IEmployeeManagementDao.class);
        employeeDataMapper = mock(EmployeeDataMapper.class);
        queryMapper = mock(QueryMapper.class);
        exceptionMapper = mockExceptionMapper();
        sut = new EmployeeManagementService(
                employeeRepository,
                employeeDataMapper,
                exceptionMapper,
                queryMapper
        );
    }


    @Test
    void getEmployeeById_shouldReturnEmployeeDetail_whenEmployeeExists() {

        Employee emp = Employee.builder()
                .id("123")
                .email("test@gmail.com")
                .fullName("John Doe")
                .build();

        EmployeeDetail mappedDetail = EmployeeDetail.builder()
                .id("123")
                .email("test@gmail.com")
                .fullName("John Doe")
                .build();

        when(employeeRepository.getEmployeeById("123"))
                .thenReturn(Either.right(Option.some(emp)));

        when(employeeDataMapper.toDetail(emp)).thenReturn(mappedDetail);

        EmployeeDetail result = sut.getEmployeeById("123");

        assertEquals("123", result.getId());
        assertEquals("John Doe", result.getFullName());
        verify(employeeDataMapper, times(1)).toDetail(emp);
    }

    @Test
    void getEmployeeById_shouldThrowNotFound_whenEmployeeMissing() {

        when(employeeRepository.getEmployeeById("999"))
                .thenReturn(Either.right(Option.none()));

        assertThatThrownBy(() -> sut.getEmployeeById("999"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertEquals(404, appEx.getErrorDetail().getCode());
                    assertEquals(ErrorCode.RESOURCE_NOT_FOUND, appEx.getErrorDetail().getErrorCode());
                });
    }

    @Test
    void getEmployeeById_shouldThrowMappedException_whenRepositoryFails() {

        OperationFailure failure = new SystemFailure(
                List.of(
                        ErrorDetail.builder()
                                .code("ERR_DB")
                                .message("DB error")
                                .build()
                ),
                new RuntimeException("DB crashed"),
                "Database failure"
        );

        AppException mapped = AppException.of(
                ErrorResponse.builder()
                        .code(500)
                        .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                        .message("Database failure")
                        .errorDetails(List.of(
                                ErrorDetail.builder()
                                        .code("ERR_DB")
                                        .message("DB error")
                                        .build()
                        ))
                        .build()
        );


        when(employeeRepository.getEmployeeById("123"))
                .thenReturn(Either.left(failure));

        when(exceptionMapper.map(failure)).thenReturn(mapped);

        assertThatThrownBy(() -> sut.getEmployeeById("123"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertEquals("Database failure", appEx.getErrorDetail().getMessage());
                });
        verify(exceptionMapper, times(1)).map(failure);
    }

    @Test
    void getAllEmployee_shouldReturnMappedList_whenEmployeesExist() {

        // ---------- Arrange ----------
        QueryRequest request = new QueryRequest(); // <-- real request

        List<Employee> employees = List.of(
                Employee.builder().id("1").fullName("A").email("a@gmail.com").build(),
                Employee.builder().id("2").fullName("B").email("b@gmail.com").build()
        );

        List<EmployeeDetail> mapped = List.of(
                EmployeeDetail.builder().id("1").fullName("A").email("a@gmail.com").build(),
                EmployeeDetail.builder().id("2").fullName("B").email("b@gmail.com").build()
        );

        Query<EmployeeSortField> defaultQuery = Query.<EmployeeSortField>defaultQuery()
                .getOrElseThrow(() -> new RuntimeException("default query failed"));

        // mock transformation from request -> domain Query
        when(queryMapper.transform(eq(request), eq(exceptionMapper)))
                .thenReturn(defaultQuery);

        // mock repo getting employees for that query
        when(employeeRepository.getEmployees(defaultQuery))
                .thenReturn(Either.right(employees));

        // Mock detail mapper
        when(employeeDataMapper.toDetail(employees.get(0))).thenReturn(mapped.get(0));
        when(employeeDataMapper.toDetail(employees.get(1))).thenReturn(mapped.get(1));

        // ---------- Act ----------
        List<EmployeeDetail> result = sut.getAllEmployee(request);

        // ---------- Assert ----------
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getFullName());
        assertEquals("B", result.get(1).getFullName());

        verify(queryMapper).transform(eq(request), eq(exceptionMapper));
        verify(employeeRepository).getEmployees(defaultQuery);
        verify(employeeDataMapper, times(2)).toDetail(any(Employee.class));
    }



    @Test
    void getAllEmployee_shouldThrowMappedException_whenRepositoryFails() {

        // ---------- Arrange ----------
        QueryRequest request = new QueryRequest(); // real request

        OperationFailure failure = new SystemFailure(
                List.of(
                        ErrorDetail.builder()
                                .code("ERR_DB")
                                .message("DB error")
                                .build()
                ),
                new RuntimeException("DB crashed"),
                "Database failure"
        );

        AppException mapped = AppException.of(
                ErrorResponse.builder()
                        .message("DB failed")
                        .code(500)
                        .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                        .build()
        );

        // Mock the query mapping
        Query<EmployeeSortField> defaultQuery = Query.<EmployeeSortField>defaultQuery()
                .getOrElseThrow(f -> new RuntimeException("default query failed"));

        when(queryMapper.transform(eq(request), eq(exceptionMapper)))
                .thenReturn(defaultQuery);

        // Mock repository failure
        when(employeeRepository.getEmployees(defaultQuery))
                .thenReturn(Either.left(failure));

        // Mock exception mapping
        when(exceptionMapper.mapAndThrow(failure))
                .thenThrow(mapped);

        // ---------- Act + Assert ----------
        assertThatThrownBy(() -> sut.getAllEmployee(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertEquals("DB failed", appEx.getErrorDetail().getMessage());
                });

        verify(queryMapper).transform(eq(request), eq(exceptionMapper));
        verify(employeeRepository).getEmployees(defaultQuery);
        verify(exceptionMapper).mapAndThrow(failure);
    }


    @Test
    void getAllEmployee_shouldReturnEmptyList_whenNoEmployees() {

        // -------- Arrange --------
        QueryRequest request = new QueryRequest();

        // mock query returned by QueryMapper
        Query<EmployeeSortField> defaultQuery = Query.<EmployeeSortField>defaultQuery()
                .getOrElseThrow(f -> new RuntimeException("default query failed"));

        when(queryMapper.transform(eq(request), eq(exceptionMapper)))
                .thenReturn(defaultQuery);

        when(employeeRepository.getEmployees(defaultQuery))
                .thenReturn(Either.right(List.of()));

        // -------- Act --------
        List<EmployeeDetail> result = sut.getAllEmployee(request);

        // -------- Assert --------
        assertTrue(result.isEmpty());

        verify(employeeDataMapper, never()).toDetail(any());
        verify(queryMapper).transform(eq(request), eq(exceptionMapper));
        verify(employeeRepository).getEmployees(defaultQuery);
    }



    @Test
    void deleteEmployee_shouldDoNothing_whenSuccessful() {

        // Repository returns Option.none() → success
        when(employeeRepository.deleteEmployee("123"))
                .thenReturn(Option.none());

        sut.deleteEmployee("123");

        // Ensure exceptionMapper is never called
        verify(exceptionMapper, never()).mapAndThrow(any());

        verify(employeeRepository, times(1)).deleteEmployee("123");
    }

    @Test
    void createEmployee_shouldReturnEmployeeDetail_whenSuccessful() {
        // Input DTO
        EmployeeDetailInput input = EmployeeDetailInput.builder()
                .fullName("John Doe")
                .email("john@gmail.com")
                .build();

        // Mapped domain entity
        Employee entity = Employee.builder()
                .id("123")
                .fullName("John Doe")
                .email("john@gmail.com")
                .build();

        // Mapped DTO to return
        EmployeeDetail detail = EmployeeDetail.builder()
                .id("123")
                .fullName("John Doe")
                .email("john@gmail.com")
                .build();

        // Mock data mapper
        when(employeeDataMapper.toEntity(input)).thenReturn(entity);

        // Mock repository
        when(employeeRepository.createEmployee(entity))
                .thenReturn(Either.right(entity));

        // Mock mapper from Employee -> EmployeeDetail
        when(employeeDataMapper.toDetail(entity)).thenReturn(detail);

        // Call service
        EmployeeDetail result = sut.createEmployee(input);

        // Assertions
        assertEquals("123", result.getId());
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@gmail.com", result.getEmail());

        // Verify mocks
        verify(employeeDataMapper).toEntity(input);
        verify(employeeRepository).createEmployee(entity);
        verify(employeeDataMapper).toDetail(entity);
    }

    @Test
    void createEmployee_shouldThrowMappedException_whenRepositoryFails() {
        EmployeeDetailInput input = EmployeeDetailInput.builder()
                .fullName("John Doe")
                .email("john@gmail.com")
                .build();

        Employee entity = Employee.builder().build();
        when(employeeDataMapper.toEntity(input)).thenReturn(entity);

        OperationFailure failure = new SystemFailure(
                List.of(
                        ErrorDetail.builder()
                                .code("ERR_DB")
                                .message("DB error")
                                .build()
                ),
                new RuntimeException("DB crashed"),
                "Database failure"
        );
        AppException mapped = AppException.of(
                ErrorResponse.builder()
                        .message("DB error")
                        .code(500)
                        .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                        .build()
        );


        when(employeeRepository.createEmployee(entity)).thenReturn(Either.left(failure));
        when(exceptionMapper.mapAndThrow(failure)).thenThrow(mapped);

        // Call service and assert exception
        assertThatThrownBy(() -> sut.createEmployee(input))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException app = (AppException) ex;
                    assertEquals("DB error", app.getErrorDetail().getMessage());
                });

        verify(employeeDataMapper).toEntity(input);
        verify(employeeRepository).createEmployee(entity);
        verify(exceptionMapper).mapAndThrow(failure);
    }

    @Test
    void updateEmployee_shouldReturnEmployeeDetail_whenSuccessful() {
        // Input DTO
        EmployeeDetailInput input = EmployeeDetailInput.builder()
                .fullName("Jane Doe")
                .email("jane@gmail.com")
                .build();

        // Mapped domain entity
        Employee entity = Employee.builder()
                .id("123")
                .fullName("Jane Doe")
                .email("jane@gmail.com")
                .build();

        // Expected DTO result
        EmployeeDetail detail = EmployeeDetail.builder()
                .id("123")
                .fullName("Jane Doe")
                .email("jane@gmail.com")
                .build();

        // Mock data mapper
        when(employeeDataMapper.toEntity(input)).thenReturn(entity);

        // Mock repository update
        when(employeeRepository.updateEmployee("123", entity)).thenReturn(Either.right(entity));

        // Mock mapper from Employee -> EmployeeDetail
        when(employeeDataMapper.toDetail(entity)).thenReturn(detail);

        // Call service
        EmployeeDetail result = sut.updateEmployee("123", input);

        // Assertions
        assertEquals("123", result.getId());
        assertEquals("Jane Doe", result.getFullName());
        assertEquals("jane@gmail.com", result.getEmail());

        // Verify mocks
        verify(employeeDataMapper).toEntity(input);
        verify(employeeRepository).updateEmployee("123", entity);
        verify(employeeDataMapper).toDetail(entity);
    }

    @Test
    void updateEmployee_shouldThrowMappedException_whenRepositoryFails() {
        EmployeeDetailInput input = EmployeeDetailInput.builder()
                .fullName("Jane Doe")
                .email("jane@gmail.com")
                .build();

        Employee entity = Employee.builder().build();
        when(employeeDataMapper.toEntity(input)).thenReturn(entity);

        OperationFailure failure = new SystemFailure(
                List.of(
                        ErrorDetail.builder()
                                .code("ERR_DB")
                                .message("DB error")
                                .build()
                ),
                new RuntimeException("DB crashed"),
                "Database failure"
        );
        AppException mapped = AppException.of(
                ErrorResponse.builder()
                        .message("Update failed")
                        .code(500)
                        .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                        .build()
        );


        when(employeeRepository.updateEmployee("123", entity)).thenReturn(Either.left(failure));
        when(exceptionMapper.mapAndThrow(failure)).thenThrow(mapped);

        // Call service and assert exception
        assertThatThrownBy(() -> sut.updateEmployee("123", input))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException app = (AppException) ex;
                    assertEquals("Update failed", app.getErrorDetail().getMessage());
                });

        verify(employeeDataMapper).toEntity(input);
        verify(employeeRepository).updateEmployee("123", entity);
        verify(exceptionMapper).mapAndThrow(failure);
    }


    @Test
    void deleteEmployee_shouldThrowMappedException_whenFailureReturned() {

        OperationFailure failure = new SystemFailure(
                List.of(
                        ErrorDetail.builder()
                                .code("ERR_DB")
                                .message("DB error")
                                .build()
                ),
                new RuntimeException("DB crashed"),
                "Database failure"
        );
        AppException mapped = AppException.of(
                ErrorResponse.builder()
                        .message("Delete failed")
                        .code(500)
                        .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                        .build()
        );


        // Repository returns failure
        when(employeeRepository.deleteEmployee("123"))
                .thenReturn(Option.some(failure));

        // exceptionMapper throws mapped AppException
        when(exceptionMapper.mapAndThrow(failure)).thenThrow(mapped);

        assertThatThrownBy(() -> sut.deleteEmployee("123"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException app = (AppException) ex;
                    assertEquals("Delete failed", app.getErrorDetail().getMessage());
                });

        verify(exceptionMapper, times(1)).mapAndThrow(failure);
        verify(employeeRepository, times(1)).deleteEmployee("123");
    }

    @Test
    void deleteEmployee_shouldAlwaysCallRepository() {

        when(employeeRepository.deleteEmployee("999"))
                .thenReturn(Option.none());

        sut.deleteEmployee("999");

        verify(employeeRepository, times(1)).deleteEmployee("999");
    }


}