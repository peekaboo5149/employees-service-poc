package com.deloitte.employee.infra.dao;

import com.deloitte.employee.domain.entities.Employee;
import com.deloitte.employee.domain.enums.EmployeeSortField;
import com.deloitte.employee.domain.entities.ErrorDetail;
import com.deloitte.employee.domain.failure.*;
import com.deloitte.employee.domain.repository.IEmployeeManagementDao;
import com.deloitte.employee.domain.support.EntitySearchValidationSupport;
import com.deloitte.employee.domain.support.EntitySortSupport;
import com.deloitte.employee.domain.valueobject.Query;
import com.deloitte.employee.domain.valueobject.Search;
import com.deloitte.employee.domain.enums.SortDirection;
import com.deloitte.employee.infra.entities.EmployeeJPAEntity;
import com.deloitte.employee.infra.mapper.EmployeeJPAMapper;
import com.deloitte.employee.infra.repositories.EmployeeJPARepository;
import io.vavr.control.Either;
import io.vavr.control.Option;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class EmployeeManagementDao implements IEmployeeManagementDao,
        EntitySortSupport<EmployeeSortField>,
        EntitySearchValidationSupport<EmployeeJPAEntity> {

    private final EmployeeJPARepository employeeJPARepository;
    private final EmployeeJPAMapper employeeJPAMapper;

    @Override
    public Either<OperationFailure, List<Employee>> getEmployees(Query<EmployeeSortField> query) {
        try {
            var sort =
                    Sort.by(
                            Sort.Direction.ASC,
                            "createdAt"
                    );

            if (query.sorts() != null && !query.sorts().isEmpty()) {
                List<Sort.Order> orders = query.sorts().stream()
                        .map(s -> new Sort.Order(
                                s.direction() == SortDirection.ASC
                                        ? Sort.Direction.ASC
                                        : Sort.Direction.DESC,
                                mapSortFieldToColumn(s.field())
                        ))
                        .toList();

                sort = Sort.by(orders);
            }

            var pageReq = query.pageRequest();
            var pageable =
                    org.springframework.data.domain.PageRequest.of(
                            pageReq.page(),
                            pageReq.size(),
                            sort
                    );

            final Either<ValidationFailure, Search> searchResult = validateSearch(query.search(), EmployeeJPAEntity.class);

            if (searchResult.isLeft()) return Either.left(searchResult.getLeft());
            Search search = searchResult.get();

            var spec = buildSearchSpecification(search);
            var page = (spec == null)
                    ? employeeJPARepository.findAll(pageable)
                    : employeeJPARepository.findAll(spec, pageable);

            var employees = page
                    .stream()
                    .map(employeeJPAMapper::toDomain)
                    .toList();

            return Either.right(employees);

        } catch (Throwable e) {
            return Either.left(new InfraStructureFailure(
                    List.of(
                            ErrorDetail.builder()
                                    .code("ERR_DB")
                                    .message(e.getMessage())
                                    .field("database")
                                    .build()
                    )
            ));
        }
    }


    private Specification<EmployeeJPAEntity> buildSearchSpecification(Search search) {
        if (search == null || !search.hasCriteria()) return null;

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            search.getCriteria().forEach((field, value) -> {
                // Case-insensitive contains
                predicates.add(cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%"));
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    @Override
    public Either<OperationFailure, Option<Employee>> getEmployeeById(String id) {
        try {
            return Either.right(
                    Option.of(employeeJPARepository.findById(id)
                            .map(employeeJPAMapper::toDomain)
                            .orElse(null))
            );
        } catch (Throwable e) {
            return Either.left(new InfraStructureFailure(
                    List.of(
                            ErrorDetail.builder()
                                    .code("ERR_DB")
                                    .message(e.getMessage())
                                    .field("database")
                                    .build()
                    )
            ));
        }
    }

    @Override
    public Either<OperationFailure, Employee> createEmployee(Employee employee) {

        try {
            String email = employee.getEmail();
            if (employeeJPARepository.existsByEmail(email)) {
                return Either.left(
                        new ResourceConflictFailure(List.of(
                                ErrorDetail.builder().field("email").code("ERR_EMAIL_EXISTS").message("Email already exists").build()
                        ))
                );
            }
            return Either.right(employeeJPAMapper.toDomain(employeeJPARepository.save(employeeJPAMapper.toEntity(employee))));
        } catch (Throwable e) {
            return getException(e);
        }
    }

    @Transactional
    @Override
    public Either<OperationFailure, Employee> updateEmployee(String id, Employee employee) {
        try {
            var employeeEntity = employeeJPARepository.findById(id);
            if (employeeEntity.isEmpty()) {
                return Either.left(new ResourceNotFoundFailure(List.of(ErrorDetail.builder().field("id").code("ERR_EMPLOYEE_NOT_FOUND").message("Employee not found").build())));
            }

            EmployeeJPAEntity updatedEntity = employeeJPAMapper.merge(employeeEntity.get(), employee);
            var result = employeeJPARepository.save(updatedEntity);
            return Either.right(employeeJPAMapper.toDomain(result));
        } catch (Throwable e) {
            return getException(e);
        }
    }

    @Transactional
    @Override
    public Option<OperationFailure> deleteEmployee(String id) {
        try {
            if (employeeJPARepository.existsById(id)) {
                employeeJPARepository.deleteById(id);
                return Option.none();
            } else {
                return Option.some(new ResourceNotFoundFailure(List.of(ErrorDetail.builder().field("id").code("ERR_EMPLOYEE_NOT_FOUND").message("Employee not found").build())));
            }

        } catch (Throwable e) {
            return Option.some(new InfraStructureFailure(
                    List.of(
                            ErrorDetail.builder()
                                    .code("ERR_DB")
                                    .message(e.getMessage())
                                    .field("database")
                                    .build()
                    )
            ));
        }
    }

    private static Either<OperationFailure, Employee> getException(Throwable e) {
        if (e instanceof DuplicateKeyException || e.getMessage().contains("duplicate key value violates unique constraint")) {
            return Either.left(new ValidationFailure(
                    List.of(
                            ErrorDetail.builder()
                                    .field("key")
                                    .code("ERR_DUPLICATE_KEY")
                                    .message(e.getMessage())
                                    .build()
                    )
            ));
        }

        return Either.left(new InfraStructureFailure(
                List.of(
                        ErrorDetail.builder()
                                .code("ERR_DB")
                                .message(e.getMessage())
                                .field("database")
                                .build()
                )
        ));
    }

    @Override
    public String mapSortFieldToColumn(EmployeeSortField field) {
        return switch (field) {
            case EMAIL -> "email";
            case FULL_NAME -> "fullName";
            case CREATED_AT -> "createdAt";
            case UPDATED_AT -> "updatedAt";
            case DESIGNATION -> "designation";
        };
    }

}
