package com.deloitte.employee.presentation.mapper;

import com.deloitte.employee.domain.entities.ErrorDetail;
import com.deloitte.employee.domain.enums.EmployeeSortField;
import com.deloitte.employee.domain.enums.SortDirection;
import com.deloitte.employee.domain.failure.OperationFailure;
import com.deloitte.employee.domain.failure.ValidationFailure;
import com.deloitte.employee.domain.mapper.ExceptionMapper;
import com.deloitte.employee.domain.valueobject.PageResult;
import com.deloitte.employee.domain.valueobject.Query;
import com.deloitte.employee.domain.valueobject.Search;
import com.deloitte.employee.domain.valueobject.SortSpec;
import com.deloitte.employee.presentation.dto.request.EmployeeQueryRequest;
import com.deloitte.employee.presentation.exception.AppException;
import io.vavr.control.Either;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class QueryMapper {
    public Query<EmployeeSortField> transform(EmployeeQueryRequest req, ExceptionMapper<AppException> exceptionMapper) {
        Query<EmployeeSortField> defaultQuery = Query.<EmployeeSortField>defaultQuery().fold(
                exceptionMapper::mapAndThrow,
                q -> q
        );

        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null ? req.getSize() : 20;

        PageResult pageResult = PageResult.of(page, size)
                .fold(exceptionMapper::mapAndThrow, p -> p);

        List<SortSpec<EmployeeSortField>> sortSpecs = new ArrayList<>();

        if (req.getSorts() != null) {
            for (var sort : req.getSorts()) {
                try {
                    EmployeeSortField field = EmployeeSortField.valueOf(sort.getField());
                    SortDirection direction = SortDirection.valueOf(sort.getDirection());
                    Either<OperationFailure, SortSpec<EmployeeSortField>> res = switch (direction) {
                        case ASC -> SortSpec.asc(field);
                        case DESC -> SortSpec.desc(field);
                    };
                    SortSpec<EmployeeSortField> spec = res.fold(exceptionMapper::mapAndThrow, s -> s);
                    sortSpecs.add(spec);
                } catch (IllegalArgumentException ex) {
                    throw new ValidationFailure(
                            List.of(ErrorDetail.builder()
                                    .field("sorts")
                                    .message(ex.getMessage())
                                    .code("ERR_INVALID_SORT_FIELD")
                                    .build())
                    );
                }
            }
        }

        Search search = Search.empty();

        Map<String, String> searchMap = req.getSearch();
        if (searchMap != null) {
            searchMap.forEach(search::add);
        }

        Either<OperationFailure, Query<EmployeeSortField>> queryResult =
                Query.of(pageResult, sortSpecs, search);

        return queryResult.fold(
                exceptionMapper::mapAndThrow,
                q -> q
        );
    }

}
