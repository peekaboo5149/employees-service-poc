package com.deloitte.employee.domain.failure;

import com.deloitte.employee.domain.entities.ErrorDetail;

import java.util.List;

public final class ResourceNotFoundFailure extends OperationFailure {

    public ResourceNotFoundFailure(
            List<ErrorDetail> errorDetail,
            Throwable cause,
            String message
    ) {
        super(errorDetail, cause, message);
    }

    public ResourceNotFoundFailure(
            List<ErrorDetail> errorDetail
    ) {
        super(errorDetail, null, "Resource not found failure occurred.");
    }
}
