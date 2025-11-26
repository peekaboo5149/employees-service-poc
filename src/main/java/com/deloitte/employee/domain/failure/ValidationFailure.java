package com.deloitte.employee.domain.failure;

import com.deloitte.employee.domain.entities.ErrorDetail;

import java.util.List;

public final class ValidationFailure extends OperationFailure {
    public ValidationFailure(
            List<ErrorDetail> errorDetail,
            Throwable cause,
            String message
    ) {
        super(errorDetail, cause, message);
    }

    public ValidationFailure(List<ErrorDetail> errorDetail) {
        super(errorDetail, null, "Validation failure occurred.");
    }
}
