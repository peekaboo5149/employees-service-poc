package com.deloitte.employee.domain.valueobject;

import com.deloitte.employee.domain.entities.ErrorDetail;
import com.deloitte.employee.domain.failure.OperationFailure;
import com.deloitte.employee.domain.failure.ValidationFailure;
import io.vavr.control.Either;

import java.util.ArrayList;
import java.util.List;

/**
 * @param page 0-based
 */
public record PageResult(int page, int size) {

    public int offset() {
        return page * size;
    }

    private static Either<OperationFailure, PageResult> validate(int page, int size) {
        List<ErrorDetail> errors = new ArrayList<>();

        if (page < 0) {
            errors.add(ErrorDetail.builder()
                    .field("page")
                    .message("Page index cannot be negative")
                    .code("ERR_NEGATIVE_PAGE")
                    .build());
        }

        if (size <= 0) {
            errors.add(ErrorDetail.builder()
                    .field("size")
                    .message("Page size must be greater than 0")
                    .code("ERR_INVALID_PAGE_SIZE")
                    .build());
        }

        if (size > 1000) {
            errors.add(ErrorDetail.builder()
                    .field("size")
                    .message("Page size too large. Maximum allowed is 1000")
                    .code("ERR_PAGE_SIZE_TOO_LARGE")
                    .build());
        }

        if (!errors.isEmpty()) {
            return Either.left(new ValidationFailure(errors));
        }

        return Either.right(new PageResult(page, size));
    }

    public static Either<OperationFailure, PageResult> of(int page, int size) {
        return validate(page, size);
    }

    public static Either<OperationFailure, PageResult> defaultPage() {
        return validate(0, 20);
    }
}
