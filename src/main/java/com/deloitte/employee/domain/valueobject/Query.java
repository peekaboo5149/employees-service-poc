package com.deloitte.employee.domain.valueobject;

import com.deloitte.employee.domain.entities.ErrorDetail;
import com.deloitte.employee.domain.failure.OperationFailure;
import com.deloitte.employee.domain.failure.ValidationFailure;
import io.vavr.control.Either;

import java.util.ArrayList;
import java.util.List;

public record Query<SORT_FIELD extends Enum<?>>(PageResult pageRequest,
                                                List<SortSpec<SORT_FIELD>> sorts,
                                                Search search
) {

    public static <SORT_FIELD extends Enum<?>> Either<OperationFailure, Query<SORT_FIELD>> of(
            PageResult pageRequest,
            List<SortSpec<SORT_FIELD>> sorts
    ) {
        List<ErrorDetail> errors = new ArrayList<>();

        if (pageRequest == null) {
            errors.add(ErrorDetail.builder()
                    .field("pageRequest")
                    .message("Page request cannot be null")
                    .code("ERR_NULL_PAGE_REQUEST")
                    .build());
        }

        if (sorts == null) {
            errors.add(ErrorDetail.builder()
                    .field("sorts")
                    .message("Sort list cannot be null")
                    .code("ERR_NULL_SORT_LIST")
                    .build());
        }

        if (!errors.isEmpty()) {
            return Either.left(new ValidationFailure(errors));
        }

        return Either.right(new Query<>(pageRequest, sorts, Search.NEW));
    }

    public static <SORT_FIELD extends Enum<?>> Either<OperationFailure, Query<SORT_FIELD>> of(
            PageResult pageRequest,
            List<SortSpec<SORT_FIELD>> sorts,
            Search search
    ) {
        List<ErrorDetail> errors = new ArrayList<>();

        if (pageRequest == null) {
            errors.add(ErrorDetail.builder()
                    .field("pageRequest")
                    .message("Page request cannot be null")
                    .code("ERR_NULL_PAGE_REQUEST")
                    .build());
        }

        if (sorts == null) {
            errors.add(ErrorDetail.builder()
                    .field("sorts")
                    .message("Sort list cannot be null")
                    .code("ERR_NULL_SORT_LIST")
                    .build());
        }

        if (search == null) search = Search.NEW;

        if (!errors.isEmpty()) {
            return Either.left(new ValidationFailure(errors));
        }

        return Either.right(new Query<>(pageRequest, sorts, search));
    }


    public static <SORT_FIELD extends Enum<?>> Either<OperationFailure, Query<SORT_FIELD>> defaultQuery() {
        return PageResult.defaultPage()
                .map(page -> new Query<>(page, List.of(), Search.NEW));
    }
}
