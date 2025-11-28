package com.deloitte.employee.infra.dao;

import com.deloitte.employee.domain.entities.Employee;
import com.deloitte.employee.domain.enums.EmployeeSortField;
import com.deloitte.employee.domain.failure.*;
import com.deloitte.employee.domain.valueobject.PageResult;
import com.deloitte.employee.domain.valueobject.Query;
import com.deloitte.employee.domain.valueobject.Search;
import com.deloitte.employee.domain.valueobject.SortSpec;
import com.deloitte.employee.infra.mapper.EmployeeJPAMapper;
import com.deloitte.employee.infra.repositories.EmployeeJPARepository;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.deloitte.employee.helper.TestUtils.getLexicographicalFullNames;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@DataJpaTest
@Import({EmployeeManagementDao.class, EmployeeJPAMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
class EmployeeManagementDaoTest {

    @Autowired
    private EmployeeManagementDao employeeManagementDao;

    @Autowired
    private EmployeeJPARepository employeeJPARepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Employee employee0;
    private Employee employee1;
    private Employee employee2;
    private Employee manager0;
    private Employee manager1;
    private List<Employee> employees;

    @BeforeEach
    void setUp() {
        employeeJPARepository.deleteAll();

        employee0 = buildEmployee();
        employee1 = buildEmployee();
        employee2 = buildEmployee();
        manager0 = buildEmployee();
        manager1 = buildEmployee();
        employees = new ArrayList<>();
        String[] lexicographicalFullNames = getLexicographicalFullNames(100);
        for (int i = 0; i < 100; i++) {
            employees.add(buildEmployee(lexicographicalFullNames[i], i));
        }
    }

    private Employee buildEmployee(String name, int index) {
        return Employee.builder()
                .id(UUID.randomUUID().toString())
                .email(name + UUID.randomUUID().toString().substring(0, 7) + "@example.com") // ✅ UNIQUE ALWAYS
                .password("password")
                .fullName(name)
                .phoneNumber("1234567890")
                .dob(LocalDate.of(1990, 1, 1))
                .isActive(true)
                .designation("Developer")
                .managerId(null)
                .address("123 Street")
                .createdAt(LocalDateTime.now().plusNanos(index))
                .updatedAt(LocalDateTime.now().plusNanos(index))
                .createdBy("admin")
                .updatedBy("admin")
                .build();
    }


    private Employee buildEmployee() {
        return Employee.builder()
                .id(UUID.randomUUID().toString())
                .email("user_" + UUID.randomUUID() + "@example.com") // ✅ UNIQUE ALWAYS
                .password("password")
                .fullName("John Doe")
                .phoneNumber("1234567890")
                .dob(LocalDate.of(1990, 1, 1))
                .isActive(true)
                .designation("Developer")
                .managerId(null)
                .address("123 Street")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("admin")
                .updatedBy("admin")
                .build();
    }

    // ---------------- CREATE ----------------

    @Test
    void createEmployee_shouldSaveEmployee() {
        Either<OperationFailure, Employee> result = employeeManagementDao.createEmployee(employee0);
        assertThat(result.isRight()).isTrue();

        employeeJPARepository.flush();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees WHERE id = ?",
                Integer.class,
                employee0.getId()
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void createEmployee_shouldFailIfEmailExists() {
        employeeManagementDao.createEmployee(employee0);
        employeeJPARepository.flush();

        Employee duplicateEmployee = employee0.toBuilder()
                .id(UUID.randomUUID().toString())
                .build();

        Either<OperationFailure, Employee> result =
                employeeManagementDao.createEmployee(duplicateEmployee);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(ResourceConflictFailure.class);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createEmployee_shouldHandleDatabaseErrorGracefully() {
        destroyTable();
        Either<OperationFailure, Employee> result =
                employeeManagementDao.createEmployee(employee0);
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(InfraStructureFailure.class);
        recreateTable();
    }

    // ---------------- GET ALL ----------------

    private static <T extends Enum<?>> Query<T> defaultQuery() {
        return Query.<T>defaultQuery().fold(
                f -> {
                    throw new IllegalArgumentException();
                },
                q -> q
        );
    }


    @Test
    void getEmployees_shouldReturnEmptyList_whenNoEmployeesExist() {
        Either<OperationFailure, List<Employee>> result = employeeManagementDao.getEmployees(defaultQuery());
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void getEmployees_shouldReturnAllEmployees_whenEmployeesExist() {
        saveEmployeeWithoutManager(employee1);
        saveEmployeeWithoutManager(employee2);

        Either<OperationFailure, List<Employee>> result = employeeManagementDao.getEmployees(defaultQuery());

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(2);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getEmployees_shouldHandleDatabaseErrorGracefully() {
        destroyTable();

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(defaultQuery());

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(InfraStructureFailure.class);

        recreateTable();
    }

    @Test
    void getEmployees_shouldReturnSortedEmployeesAscending() {

        // Save all employees
        employees.forEach(this::saveEmployeeWithoutManager);

        // Verify count
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees",
                Integer.class
        );
        assertThat(count).isEqualTo(employees.size());

        // Build query with ASC sort
        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.asc(EmployeeSortField.FULL_NAME).get();

        Query<EmployeeSortField> query = Query.of(
                PageResult.defaultPage().get(),
                List.of(sortSpec)
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to get query");
        });

        // Execute
        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        // Assertions
        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();

        assertThat(page).hasSize(query.pageRequest().size());

        for (int i = 0; i < page.size() - 1; i++) {
            String current = page.get(i).getFullName();
            String next = page.get(i + 1).getFullName();
            assertThat(current.compareTo(next)).isLessThanOrEqualTo(0);
        }
        employeeJPARepository.flush();
        // Also lets do a real jdbc query and check if the above match
        List<String> dbSortedNames = jdbcTemplate.query(
                """
                        SELECT full_name
                        FROM employees
                        ORDER BY full_name ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getString("full_name"),
                query.pageRequest().size(),
                query.pageRequest().offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getFullName())
                    .isEqualTo(dbSortedNames.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnSortedEmployeesDescendingByFullName() {

        employees.forEach(this::saveEmployeeWithoutManager);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees",
                Integer.class
        );
        assertThat(count).isEqualTo(employees.size());

        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.desc(EmployeeSortField.FULL_NAME).get();

        Query<EmployeeSortField> query = Query.of(
                PageResult.defaultPage().get(),
                List.of(sortSpec)
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to get query");
        });

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();
        assertThat(page).hasSize(query.pageRequest().size());

        for (int i = 0; i < page.size() - 1; i++) {
            String current = page.get(i).getFullName();
            String next = page.get(i + 1).getFullName();
            assertThat(current.compareTo(next)).isGreaterThanOrEqualTo(0);
        }

        employeeJPARepository.flush();

        List<String> dbSortedNames = jdbcTemplate.query(
                """
                        SELECT full_name
                        FROM employees
                        ORDER BY full_name DESC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getString("full_name"),
                query.pageRequest().size(),
                query.pageRequest().offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getFullName())
                    .isEqualTo(dbSortedNames.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnSortedEmployeesAscendingByCreatedAt() {

        employees.forEach(this::saveEmployeeWithoutManager);

        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.asc(EmployeeSortField.CREATED_AT).get();

        Query<EmployeeSortField> query = Query.of(
                PageResult.defaultPage().get(),
                List.of(sortSpec)
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to get query");
        });

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();

        for (int i = 0; i < page.size() - 1; i++) {
            assertThat(page.get(i).getCreatedAt())
                    .isBeforeOrEqualTo(page.get(i + 1).getCreatedAt());
        }

        employeeJPARepository.flush();

        List<LocalDateTime> dbDates = jdbcTemplate.query(
                """
                        SELECT created_at
                        FROM employees
                        ORDER BY created_at ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getTimestamp("created_at").toLocalDateTime(),
                query.pageRequest().size(),
                query.pageRequest().offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getCreatedAt())
                    .isEqualTo(dbDates.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnSortedEmployeesDescendingByCreatedAt() {

        employees.forEach(this::saveEmployeeWithoutManager);

        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.desc(EmployeeSortField.CREATED_AT).get();

        Query<EmployeeSortField> query = Query.of(
                PageResult.defaultPage().get(),
                List.of(sortSpec)
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to get query");
        });

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();

        for (int i = 0; i < page.size() - 1; i++) {
            assertThat(page.get(i).getCreatedAt())
                    .isAfterOrEqualTo(page.get(i + 1).getCreatedAt());
        }

        employeeJPARepository.flush();

        List<LocalDateTime> dbDates = jdbcTemplate.query(
                """
                        SELECT created_at
                        FROM employees
                        ORDER BY created_at DESC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getTimestamp("created_at").toLocalDateTime(),
                query.pageRequest().size(),
                query.pageRequest().offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getCreatedAt())
                    .isEqualTo(dbDates.get(i));
        }
    }


    @Test
    void getEmployees_shouldReturnSortedEmployeesAscendingByUpdatedAt() {

        employees.forEach(this::saveEmployeeWithoutManager);

        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.asc(EmployeeSortField.UPDATED_AT).get();

        Query<EmployeeSortField> query = Query.of(
                PageResult.defaultPage().get(),
                List.of(sortSpec)
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to get query");
        });

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();

        for (int i = 0; i < page.size() - 1; i++) {
            assertThat(page.get(i).getUpdatedAt())
                    .isBeforeOrEqualTo(page.get(i + 1).getUpdatedAt());
        }

        employeeJPARepository.flush();

        List<LocalDateTime> dbDates = jdbcTemplate.query(
                """
                        SELECT updated_at
                        FROM employees
                        ORDER BY updated_at ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getTimestamp("updated_at").toLocalDateTime(),
                query.pageRequest().size(),
                query.pageRequest().offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getUpdatedAt())
                    .isEqualTo(dbDates.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnSortedEmployeesAscendingByDesignation() {

        employees.forEach(this::saveEmployeeWithoutManager);

        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.asc(EmployeeSortField.DESIGNATION).get();

        Query<EmployeeSortField> query = Query.of(
                PageResult.defaultPage().get(),
                List.of(sortSpec)
        ).getOrElseThrow(
                f -> {
                    throw new RuntimeException("Failed to get query");
                }
        );

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();

        for (int i = 0; i < page.size() - 1; i++) {
            assertThat(page.get(i).getDesignation()
                    .compareTo(page.get(i + 1).getDesignation()))
                    .isLessThanOrEqualTo(0);
        }

        employeeJPARepository.flush();

        List<String> dbDesignations = jdbcTemplate.query(
                """
                        SELECT designation
                        FROM employees
                        ORDER BY designation ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getString("designation"),
                query.pageRequest().size(),
                query.pageRequest().offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getDesignation())
                    .isEqualTo(dbDesignations.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnFirstPage() {

        employees.forEach(this::saveEmployeeWithoutManager);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees",
                Integer.class
        );
        assertThat(count).isEqualTo(employees.size());

        PageResult pageRequest =
                PageResult.of(0, 10).get();

        Query<EmployeeSortField> query = Query.<EmployeeSortField>of(
                pageRequest,
                List.of()
        ).getOrElseThrow(
                f -> {
                    throw new RuntimeException("Failed to get query");
                }
        );

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();

        assertThat(page).hasSize(pageRequest.size());

        employeeJPARepository.flush();

        List<String> dbIds = jdbcTemplate.query(
                """
                        SELECT id
                        FROM employees
                        ORDER BY created_at ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getString("id"),
                pageRequest.size(),
                pageRequest.offset()
        );

        System.out.println("EXPECTED = " + dbIds);
        System.out.println("ACTUAL = " + page.stream().map(Employee::getId).toList());

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getId())
                    .isEqualTo(dbIds.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnSecondPage() {

        // arrange
        employees.forEach(this::saveEmployeeWithoutManager);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees",
                Integer.class
        );
        assertThat(count).isEqualTo(employees.size());

        PageResult pageRequest =
                PageResult.of(1, 10).get(); // second page (0-based)

        Query<EmployeeSortField> query = Query.<EmployeeSortField>of(
                pageRequest,
                List.of()
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to get query");
        });

        // act
        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        // assert domain result
        assertThat(result.isRight()).isTrue();
        List<Employee> page = result.get();
        assertThat(page).hasSize(pageRequest.size());

        // ensure DB visible state
        employeeJPARepository.flush();

        // JDBC verification
        List<String> dbIds = jdbcTemplate.query(
                """
                        SELECT id
                        FROM employees
                        ORDER BY created_at ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getString("id"),
                pageRequest.size(),
                pageRequest.offset()
        );

        // compare element-wise
        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getId()).isEqualTo(dbIds.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnLastPartialPage() {

        // arrange
        employees.forEach(this::saveEmployeeWithoutManager);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees",
                Integer.class
        );
        assertThat(count).isEqualTo(employees.size());

        int pageSize = 11; // choose a size that does not divide 100 evenly to create a partial last page
        int total = employees.size();
        int lastPageIndex = (total - 1) / pageSize; // correct last index

        PageResult pageRequest =
                PageResult.of(lastPageIndex, pageSize).get();

        Query<EmployeeSortField> query = Query.<EmployeeSortField>of(
                pageRequest,
                List.of()
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to get query");
        });

        // act
        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        // assert domain result
        assertThat(result.isRight()).isTrue();
        List<Employee> page = result.get();

        // expected remaining elements on last page
        int expectedRemaining = Math.max(0, total - pageRequest.offset());
        int expectedSize = Math.min(pageSize, expectedRemaining);
        assertThat(page).hasSize(expectedSize);

        employeeJPARepository.flush();

        // JDBC verification
        List<String> dbIds = jdbcTemplate.query(
                """
                        SELECT id
                        FROM employees
                        ORDER BY created_at ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getString("id"),
                pageRequest.size(),
                pageRequest.offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getId()).isEqualTo(dbIds.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnEmptyPage_whenPageBeyondData() {

        // arrange
        employees.forEach(this::saveEmployeeWithoutManager);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees",
                Integer.class
        );
        assertThat(count).isEqualTo(employees.size());

        int pageSize = 10;
        int total = employees.size();
        int overflowPage = (total / pageSize) + 5; // well beyond last page

        PageResult pageRequest =
                PageResult.of(overflowPage, pageSize).get();

        Query<EmployeeSortField> query = Query.<EmployeeSortField>of(
                pageRequest,
                List.of()
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to get query");
        });

        // act
        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        // assert domain result
        assertThat(result.isRight()).isTrue();
        List<Employee> page = result.get();
        assertThat(page).isEmpty();

        employeeJPARepository.flush();

        // JDBC verification: COUNT with LIMIT/OFFSET should be zero
        Integer dbCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM (
                          SELECT 1 FROM employees
                          ORDER BY created_at ASC
                          LIMIT ? OFFSET ?
                        ) t
                        """,
                Integer.class,
                pageRequest.size(),
                pageRequest.offset()
        );

        assertThat(dbCount).isEqualTo(0);
    }

    @Test
    void getEmployees_shouldReturnFirstPageSortedByFullNameAscending() {

        // Save all employees
        employees.forEach(this::saveEmployeeWithoutManager);

        // Verify total count
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees",
                Integer.class
        );
        assertThat(count).isEqualTo(employees.size());

        // Page request: FIRST page
        PageResult pageRequest =
                PageResult.of(0, 10).get();

        // Sort: FULL_NAME ASC
        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.asc(EmployeeSortField.FULL_NAME).get();

        Query<EmployeeSortField> query = Query.of(
                pageRequest,
                List.of(sortSpec)
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to build query");
        });

        // Execute
        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        // Assertions
        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();

        assertThat(page).hasSize(pageRequest.size());

        employeeJPARepository.flush();

        // ✅ DB verification (REAL SOURCE OF TRUTH)
        List<String> dbSortedNames = jdbcTemplate.query(
                """
                        SELECT full_name
                        FROM employees
                        ORDER BY full_name ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getString("full_name"),
                pageRequest.size(),
                pageRequest.offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getFullName())
                    .isEqualTo(dbSortedNames.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnFirstPageSortedByFullNameDescending() {

        employees.forEach(this::saveEmployeeWithoutManager);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees",
                Integer.class
        );
        assertThat(count).isEqualTo(employees.size());

        PageResult pageRequest =
                PageResult.of(0, 10).get();

        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.desc(EmployeeSortField.FULL_NAME).get();

        Query<EmployeeSortField> query = Query.of(
                pageRequest,
                List.of(sortSpec)
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to build query");
        });

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();
        assertThat(page).hasSize(pageRequest.size());

        employeeJPARepository.flush();

        List<String> dbSortedNames = jdbcTemplate.query(
                """
                        SELECT full_name
                        FROM employees
                        ORDER BY full_name DESC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getString("full_name"),
                pageRequest.size(),
                pageRequest.offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getFullName())
                    .isEqualTo(dbSortedNames.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnSecondPageSortedByFullNameAscending() {

        employees.forEach(this::saveEmployeeWithoutManager);

        PageResult pageRequest =
                PageResult.of(1, 10).get();  // ✅ SECOND PAGE

        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.asc(EmployeeSortField.FULL_NAME).get();

        Query<EmployeeSortField> query = Query.of(
                pageRequest,
                List.of(sortSpec)
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to build query");
        });

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();
        assertThat(page).hasSize(pageRequest.size());

        employeeJPARepository.flush();

        List<String> dbSortedNames = jdbcTemplate.query(
                """
                        SELECT full_name
                        FROM employees
                        ORDER BY full_name ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> rs.getString("full_name"),
                pageRequest.size(),
                pageRequest.offset()
        );

        for (int i = 0; i < page.size(); i++) {
            assertThat(page.get(i).getFullName())
                    .isEqualTo(dbSortedNames.get(i));
        }
    }

    @Test
    void getEmployees_shouldReturnSingleEmployeePerPage_whenPageSizeIsOne() {

        employees.forEach(this::saveEmployeeWithoutManager);

        PageResult pageRequest =
                PageResult.of(0, 1).get();

        SortSpec<EmployeeSortField> sortSpec =
                SortSpec.asc(EmployeeSortField.FULL_NAME).get();

        Query<EmployeeSortField> query = Query.of(
                pageRequest,
                List.of(sortSpec)
        ).getOrElseThrow(f -> {
            throw new RuntimeException("Failed to build query");
        });

        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        assertThat(result.isRight()).isTrue();

        List<Employee> page = result.get();
        assertThat(page).hasSize(1);

        employeeJPARepository.flush();

        List<String> dbSortedNames = jdbcTemplate.query(
                """
                        SELECT full_name
                        FROM employees
                        ORDER BY full_name ASC
                        LIMIT 1 OFFSET 0
                        """,
                (rs, rowNum) -> rs.getString("full_name")
        );

        assertThat(page.getFirst().getFullName())
                .isEqualTo(dbSortedNames.getFirst());
    }

    @Test
    void getEmployees_shouldReturnEmployeesMatchingFullNameSubstring() {
        // Save all employees
        employees.forEach(this::saveEmployeeWithoutManager);

        // Pick a substring from first employee's fullName
        String substring = employees.getFirst().getFullName().substring(0, 3).toLowerCase();

        // Build search
        Search search = Search.empty();
        search.add("fullName", substring);

        PageResult pageRequest = PageResult.of(0, employees.size()).get();

        Query<EmployeeSortField> query = Query.<EmployeeSortField>of(
                pageRequest,
                List.of(),
                search
        ).getOrElseThrow(f -> new RuntimeException("Failed to get query"));

        // Execute
        Either<OperationFailure, List<Employee>> result = employeeManagementDao.getEmployees(query);

        // Assertions
        assertThat(result.isRight()).isTrue();

        List<Employee> filtered = result.get();

        // Ensure all returned employees contain the substring (case-insensitive)
        for (Employee emp : filtered) {
            assertThat(emp.getFullName().toLowerCase()).contains(substring);
        }

        employeeJPARepository.flush();

        // JDBC verification
        List<String> dbFullNames = jdbcTemplate.query(
                """
                SELECT full_name
                FROM employees
                WHERE LOWER(full_name) LIKE ?
                ORDER BY created_at ASC
                """,
                (rs, rowNum) -> rs.getString("full_name"),
                "%" + substring + "%"
        );

        assertThat(filtered.stream().map(Employee::getFullName).toList())
                .containsExactlyElementsOf(dbFullNames);
    }

    @Test
    void getEmployees_shouldReturnEmployeesMatchingMultipleCriteria() {
        // Save all employees
        employees.forEach(this::saveEmployeeWithoutManager);

        // Pick substrings from two employees
        String substring1 = employees.get(0).getFullName().substring(0, 3).toLowerCase();
        String substring2 = employees.get(1).getEmail().substring(0, 5).toLowerCase();

        // Build search
        Search search = Search.empty();
        search.add("fullName", substring1);
        search.add("email", substring2);

        PageResult pageRequest = PageResult.of(0, employees.size()).get();

        Query<EmployeeSortField> query = Query.<EmployeeSortField>of(
                pageRequest,
                List.of(),
                search
        ).getOrElseThrow(f -> new RuntimeException("Failed to get query"));

        // Execute
        Either<OperationFailure, List<Employee>> result = employeeManagementDao.getEmployees(query);

        // Assertions
        assertThat(result.isRight()).isTrue();

        List<Employee> filtered = result.get();

        // Ensure all returned employees satisfy all search criteria (case-insensitive)
        for (Employee emp : filtered) {
            assertThat(emp.getFullName().toLowerCase()).contains(substring1);
            assertThat(emp.getEmail().toLowerCase()).contains(substring2);
        }

        employeeJPARepository.flush();

        // JDBC verification
        List<String> dbFullNames = jdbcTemplate.query(
                """
                SELECT full_name
                FROM employees
                WHERE LOWER(full_name) LIKE ?
                  AND LOWER(email) LIKE ?
                ORDER BY created_at ASC
                """,
                (rs, rowNum) -> rs.getString("full_name"),
                "%" + substring1 + "%",
                "%" + substring2 + "%"
        );

        assertThat(filtered.stream().map(Employee::getFullName).toList())
                .containsExactlyElementsOf(dbFullNames);
    }

    @Test
    void getEmployees_shouldReturnEmptyListWhenNoMatch() {
        // Save all employees
        employees.forEach(this::saveEmployeeWithoutManager);

        // Build search with a string that doesn't exist
        Search search = Search.empty();
        search.add("fullName", "nonexistentsubstring");

        PageResult pageRequest = PageResult.of(0, 20).get();

        Query<EmployeeSortField> query = Query.<EmployeeSortField>of(
                pageRequest,
                List.of(),
                search
        ).getOrElseThrow(f -> new RuntimeException("Failed to get query"));

        // Execute
        Either<OperationFailure, List<Employee>> result = employeeManagementDao.getEmployees(query);

        // Assertions
        assertThat(result.isRight()).isTrue();

        List<Employee> filtered = result.get();

        // Ensure the list is empty
        assertThat(filtered).isEmpty();

        employeeJPARepository.flush();

        // JDBC verification
        List<String> dbFullNames = jdbcTemplate.query(
                """
                SELECT full_name
                FROM employees
                WHERE LOWER(full_name) LIKE ?
                ORDER BY created_at ASC
                """,
                (rs, rowNum) -> rs.getString("full_name"),
                "%nonexistentsubstring%"
        );

        assertThat(dbFullNames).isEmpty();
    }

    @Test
    void getEmployees_shouldReturnEmployeesMatchingFullNameCaseInsensitive() {
        // Save all employees
        employees.forEach(this::saveEmployeeWithoutManager);

        // Pick a substring and change case
        String substring = employees.getFirst().getFullName().substring(0, 3).toUpperCase();

        // Build search
        Search search = Search.empty();
        search.add("fullName", substring);

        PageResult pageRequest = PageResult.of(0, 20).get();

        Query<EmployeeSortField> query = Query.<EmployeeSortField>of(
                pageRequest,
                List.of(),
                search
        ).getOrElseThrow(f -> new RuntimeException("Failed to get query"));

        // Execute
        Either<OperationFailure, List<Employee>> result = employeeManagementDao.getEmployees(query);

        // Assertions
        assertThat(result.isRight()).isTrue();

        List<Employee> filtered = result.get();

        // Ensure all returned employees contain the substring (case-insensitive)
        for (Employee emp : filtered) {
            assertThat(emp.getFullName().toLowerCase()).contains(substring.toLowerCase());
        }

        employeeJPARepository.flush();

        // JDBC verification
        List<String> dbFullNames = jdbcTemplate.query(
                """
                SELECT full_name
                FROM employees
                WHERE LOWER(full_name) LIKE ?
                ORDER BY created_at ASC
                LIMIT ? OFFSET ?
                """,
                (rs, rowNum) -> rs.getString("full_name"),
                "%" + substring.toLowerCase() + "%",
                pageRequest.size(),
                pageRequest.offset()
        );

        assertThat(filtered.stream().map(Employee::getFullName).toList())
                .containsExactlyElementsOf(dbFullNames);
    }

    @Test
    void getEmployees_shouldReturnValidationFailureWhenQueryInvalid() {
        // Build query with null pageRequest and null sorts
        Either<OperationFailure, Query<EmployeeSortField>> queryResult =
                Query.of(null, null);

        // Should return a ValidationFailure
        assertThat(queryResult.isLeft()).isTrue();

        OperationFailure failure = queryResult.getLeft();
        assertThat(failure).isInstanceOf(ValidationFailure.class);

        ValidationFailure validationFailure = (ValidationFailure) failure;
        assertThat(validationFailure.getErrorDetail())
                .extracting("field")
                .containsExactlyInAnyOrder("pageRequest", "sorts");

        assertThat(validationFailure.getErrorDetail())
                .extracting("code")
                .containsExactlyInAnyOrder("ERR_NULL_PAGE_REQUEST", "ERR_NULL_SORT_LIST");
    }

    @Test
    void getEmployees_shouldReturnValidationFailureForInvalidSearchField() {
        // Arrange — insert some sample employees
        employees.forEach(this::saveEmployeeWithoutManager);

        // Build a search using a field NOT present on EmployeeJPAEntity
        Search search = Search.empty();
        search.add("doesNotExistField", "value");

        PageResult pageRequest = PageResult.of(0, 10).get();

        Query<EmployeeSortField> query = Query.<EmployeeSortField>of(
                pageRequest,
                List.of(),
                search
        ).getOrElseThrow(f -> new RuntimeException("Query building should NOT fail here"));

        // ACT — DAO should validate search fields
        Either<OperationFailure, List<Employee>> result =
                employeeManagementDao.getEmployees(query);

        // ASSERT
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(ValidationFailure.class);

        ValidationFailure failure = (ValidationFailure) result.getLeft();

        assertThat(failure.getErrorDetail())
                .extracting("field", "code")
                .containsExactly(
                        tuple("doesNotExistField", "ERR_INVALID_SEARCH_FIELD")
                );
    }

    // ---------------- GET BY ID ----------------

    @Test
    void getEmployeeById_shouldReturnEmployee_whenExists() {
        saveEmployeeWithoutManager(employee0);

        Either<OperationFailure, Option<Employee>> result =
                employeeManagementDao.getEmployeeById(employee0.getId());

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isNotEmpty();
        assertThat(result.get().get().getEmail())
                .isEqualTo(employee0.getEmail());
    }

    @Test
    void getEmployeeById_shouldReturnEmpty_whenEmployeeDoesNotExist() {
        Either<OperationFailure, Option<Employee>> result =
                employeeManagementDao.getEmployeeById("does-not-exist");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getEmployeeById_shouldHandleDatabaseErrorGracefully() {
        destroyTable();

        Either<OperationFailure, Option<Employee>> result =
                employeeManagementDao.getEmployeeById(employee0.getId());

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(InfraStructureFailure.class);

        recreateTable();
    }

    // ---------------- DELETE ----------------

    @Test
    void deleteEmployee_shouldDeleteEmployee_whenExists() {
        saveEmployeeWithoutManager(employee0);

        Option<OperationFailure> result =
                employeeManagementDao.deleteEmployee(employee0.getId());

        employeeJPARepository.flush();

        assertThat(result.isEmpty()).isTrue();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employees WHERE id = ?",
                Integer.class,
                employee0.getId()
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    void deleteEmployee_shouldThrowResourceNotFoundFailure_whenEmployeeDoesNotExist() {
        Option<OperationFailure> result =
                employeeManagementDao.deleteEmployee("random-id");

        assertThat(result.isDefined()).isTrue();  // Option has value
        assertThat(result.get()).isInstanceOf(ResourceNotFoundFailure.class);
    }

    // ---------------- UPDATE ----------------
    @Test
    void updateEmployee_shouldUpdateEmployeeSuccessfully_whenEmployeeExists() {
        // ---------- ARRANGE ----------
        // 1. Insert existing employee using pure JDBC
        saveEmployeeWithoutManager(employee0);

        // 2. Create updated employee object (same ID, changed fields)
        Employee updatedEmployee = Employee.builder()
                .id(employee0.getId()) // SAME ID
                .email(employee0.getEmail()) // SAME email (must not trigger conflict)
                .password("new-password-123")
                .fullName("John Doe Updated")
                .phoneNumber("9999999999")
                .dob(employee0.getDob())
                .isActive(false)
                .designation("Senior Developer")
                .managerId(null)
                .address("Updated Address 789")
                .createdAt(employee0.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .createdBy(employee0.getCreatedBy())
                .updatedBy("system-updater")
                .build();

        // ---------- ACT ----------
        Either<OperationFailure, Employee> result =
                employeeManagementDao.updateEmployee(employee0.getId(), updatedEmployee);

        // Force DB sync before raw SQL verification
        employeeJPARepository.flush();

        // ---------- ASSERT (DOMAIN) ----------
        assertThat(result.isRight()).isTrue();

        Employee resultEmployee = result.get();
        assertThat(resultEmployee.getId()).isEqualTo(employee0.getId());
        assertThat(resultEmployee.getFullName()).isEqualTo("John Doe Updated");
        assertThat(resultEmployee.getPhoneNumber()).isEqualTo("9999999999");
        assertThat(resultEmployee.getDesignation()).isEqualTo("Senior Developer");
        assertThat(resultEmployee.isActive()).isFalse();
        assertThat(resultEmployee.getUpdatedBy()).isEqualTo("system-updater");

        // ---------- ASSERT (RAW DATABASE) ----------
        String dbFullName = jdbcTemplate.queryForObject(
                "SELECT full_name FROM employees WHERE id = ?",
                String.class,
                employee0.getId()
        );

        String dbPhone = jdbcTemplate.queryForObject(
                "SELECT phone_number FROM employees WHERE id = ?",
                String.class,
                employee0.getId()
        );

        String dbDesignation = jdbcTemplate.queryForObject(
                "SELECT designation FROM employees WHERE id = ?",
                String.class,
                employee0.getId()
        );

        Boolean dbActive = jdbcTemplate.queryForObject(
                "SELECT is_active FROM employees WHERE id = ?",
                Boolean.class,
                employee0.getId()
        );

        String dbUpdatedBy = jdbcTemplate.queryForObject(
                "SELECT updated_by FROM employees WHERE id = ?",
                String.class,
                employee0.getId()
        );

        assertThat(dbFullName).isEqualTo("John Doe Updated");
        assertThat(dbPhone).isEqualTo("9999999999");
        assertThat(dbDesignation).isEqualTo("Senior Developer");
        assertThat(dbActive).isFalse();
        assertThat(dbUpdatedBy).isEqualTo("system-updater");
    }

    @Test
    void updateEmployee_shouldUpdatePasswordOnly_whenValidNewPasswordProvided() {
        // ---------- ARRANGE ----------
        saveEmployeeWithoutManager(employee0);

        String newPassword = "newStrongPassword@123";

        Employee updatedEmployee = Employee.builder()
                .id(employee0.getId())
                .email(employee0.getEmail())
                .password(newPassword) // ✅ ONLY password change
                .fullName(employee0.getFullName())
                .phoneNumber(employee0.getPhoneNumber())
                .dob(employee0.getDob())
                .isActive(employee0.isActive())
                .designation(employee0.getDesignation())
                .managerId(employee0.getManagerId())
                .address(employee0.getAddress())
                .createdAt(employee0.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .createdBy(employee0.getCreatedBy())
                .updatedBy("password-updater")
                .build();

        // ---------- ACT ----------
        Either<OperationFailure, Employee> result =
                employeeManagementDao.updateEmployee(employee0.getId(), updatedEmployee);

        employeeJPARepository.flush();

        // ---------- ASSERT ----------
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getPassword()).isEqualTo(newPassword);

        String dbPassword = jdbcTemplate.queryForObject(
                "SELECT password FROM employees WHERE id = ?",
                String.class,
                employee0.getId()
        );

        assertThat(dbPassword).isEqualTo(newPassword);
    }

    @Test
    void updateEmployee_shouldRemoveManager_whenManagerIsSetToNull() {
        // ---------- ARRANGE ----------
        saveEmployeeWithManager(employee0, manager0);


        Employee updatedEmployee = Employee.builder()
                .id(employee0.getId())
                .email(employee0.getEmail())
                .password(employee0.getPassword())
                .fullName(employee0.getFullName())
                .phoneNumber(employee0.getPhoneNumber())
                .dob(employee0.getDob())
                .isActive(employee0.isActive())
                .designation(employee0.getDesignation())
                .managerId(null)
                .address(employee0.getAddress())
                .createdAt(employee0.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .createdBy(employee0.getCreatedBy())
                .updatedBy("manager-remover")
                .build();

        // ---------- ACT ----------
        Either<OperationFailure, Employee> result =
                employeeManagementDao.updateEmployee(employee0.getId(), updatedEmployee);

        employeeJPARepository.flush();

        // ---------- ASSERT ----------
        assertThat(result.isRight()).isTrue();

        Long dbManagerId = jdbcTemplate.queryForObject(
                "SELECT manager_id FROM employees WHERE id = ?",
                Long.class,
                employee0.getId()
        );

        assertThat(dbManagerId).isNull();
    }

    @Test
    void updateEmployee_shouldAssignManager_whenEmployeeHasNoManager() {
        // ---------- ARRANGE ----------
        // Save employee WITHOUT manager
        saveEmployeeWithoutManager(employee0);

        // Save manager
        saveEmployeeWithoutManager(manager0);

        // Update employee with new manager
        Employee updatedEmployee = Employee.builder()
                .id(employee0.getId())
                .email(employee0.getEmail())
                .password(employee0.getPassword())
                .fullName(employee0.getFullName())
                .phoneNumber(employee0.getPhoneNumber())
                .dob(employee0.getDob())
                .isActive(employee0.isActive())
                .designation(employee0.getDesignation())
                .managerId(manager0.getId())
                .address(employee0.getAddress())
                .createdAt(employee0.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .createdBy(employee0.getCreatedBy())
                .updatedBy("manager-assigner")
                .build();

        // ---------- ACT ----------
        Either<OperationFailure, Employee> result =
                employeeManagementDao.updateEmployee(employee0.getId(), updatedEmployee);

        employeeJPARepository.flush();

        // ---------- ASSERT ----------
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getManagerId()).isEqualTo(manager0.getId());

        String dbManagerId = jdbcTemplate.queryForObject(
                "SELECT manager_id FROM employees WHERE id = ?",
                String.class,
                employee0.getId()
        );

        assertThat(dbManagerId).isEqualTo(manager0.getId());
    }

    @Test
    void updateEmployee_shouldReplaceExistingManager_whenNewManagerProvided() {
        // ---------- ARRANGE ----------
        // Save employee with manager0
        saveEmployeeWithManager(employee0, manager0);

        // Save new manager1
        saveEmployeeWithoutManager(manager1);

        // Replace manager0 with manager1
        Employee updatedEmployee = Employee.builder()
                .id(employee0.getId())
                .email(employee0.getEmail())
                .password(employee0.getPassword())
                .fullName(employee0.getFullName())
                .phoneNumber(employee0.getPhoneNumber())
                .dob(employee0.getDob())
                .isActive(employee0.isActive())
                .designation(employee0.getDesignation())
                .managerId(manager1.getId())
                .address(employee0.getAddress())
                .createdAt(employee0.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .createdBy(employee0.getCreatedBy())
                .updatedBy("manager-replacer")
                .build();

        // ---------- ACT ----------
        Either<OperationFailure, Employee> result =
                employeeManagementDao.updateEmployee(employee0.getId(), updatedEmployee);

        employeeJPARepository.flush();

        // ---------- ASSERT ----------
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getManagerId()).isEqualTo(manager1.getId());

        String dbManagerId = jdbcTemplate.queryForObject(
                "SELECT manager_id FROM employees WHERE id = ?",
                String.class,
                employee0.getId()
        );

        assertThat(dbManagerId).isEqualTo(manager1.getId());
    }


    // ---------------- HELPERS ----------------

    protected void saveEmployeeWithManager(Employee employee, Employee manager) {
        saveEmployeeWithoutManager(manager);

        saveEmployeeWithoutManager(employee);

        jdbcTemplate.update("""
                        UPDATE employees
                        SET manager_id = ?
                        WHERE id = ?
                        """,
                manager.getId(),
                employee.getId()
        );

        employeeJPARepository.flush();
    }


    private void saveEmployeeWithoutManager(Employee employee) {
        jdbcTemplate.update("""
                            INSERT INTO employees
                            (id, email, password, full_name, phone_number, dob, is_active, designation,
                             manager_id, address, created_at, updated_at, created_by, updated_by)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                employee.getId(),
                employee.getEmail(),
                employee.getPassword(),
                employee.getFullName(),
                employee.getPhoneNumber(),
                employee.getDob(),
                employee.isActive(),
                employee.getDesignation(),
                null,
                employee.getAddress(),
                employee.getCreatedAt(),
                employee.getUpdatedAt(),
                employee.getCreatedBy(),
                employee.getUpdatedBy()
        );
    }

    private void recreateTable() {
        jdbcTemplate.execute("""
                    CREATE TABLE employees (
                        id VARCHAR(50) PRIMARY KEY,
                        email VARCHAR(255),
                        password VARCHAR(255),
                        full_name VARCHAR(255),
                        phone_number VARCHAR(20),
                        dob DATE,
                        is_active BOOLEAN,
                        designation VARCHAR(255),
                        manager_id VARCHAR(50),
                        address VARCHAR(1024),
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        created_by VARCHAR(50),
                        updated_by VARCHAR(50)
                    )
                """);
    }

    private void destroyTable() {
        jdbcTemplate.execute("DROP TABLE employees");
    }
}
