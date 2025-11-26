package com.deloitte.employee.domain.valueobject;

import com.deloitte.employee.domain.entities.ErrorDetail;
import com.deloitte.employee.domain.failure.OperationFailure;
import com.deloitte.employee.domain.failure.ValidationFailure;
import io.vavr.control.Either;

import java.util.ArrayList;
import java.util.List;

public record SortSpec<SORT_FIELD extends Enum<?>>(SORT_FIELD field, SortDirection direction) {

    private static <SORT_FIELD extends Enum<?>> Either<OperationFailure, SortSpec<SORT_FIELD>> validate(SORT_FIELD field, SortDirection direction) {
        List<ErrorDetail> errors = new ArrayList<>();
        if (field == null) {
            errors.add(ErrorDetail.builder()
                    .field("field")
                    .message("Sort field cannot be null")
                    .code("ERR_NULL_FIELD")
                    .build());
        }
        if (direction == null) {
            errors.add(ErrorDetail.builder()
                    .field("direction")
                    .message("Sort direction cannot be null")
                    .code("ERR_NULL_DIRECTION")
                    .build());
        }

        if (!errors.isEmpty()) {
            return Either.left(new ValidationFailure(errors));
        }
        return Either.right(new SortSpec<>(field, direction));
    }

    public static <SORT_FIELD extends Enum<?>> Either<OperationFailure, SortSpec<SORT_FIELD>> asc(SORT_FIELD field) {
        return validate(field, SortDirection.ASC);
    }

    public static <SORT_FIELD extends Enum<?>> Either<OperationFailure, SortSpec<SORT_FIELD>> desc(SORT_FIELD field) {
        return validate(field, SortDirection.DESC);
    }
}
