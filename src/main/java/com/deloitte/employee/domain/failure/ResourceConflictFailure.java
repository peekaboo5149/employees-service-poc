package com.deloitte.employee.domain.failure;

import com.deloitte.employee.domain.entities.ErrorDetail;

import java.util.List;

public final class ResourceConflictFailure extends OperationFailure {

    public ResourceConflictFailure(
            List<ErrorDetail> errorDetail,
            Throwable cause,
            String message
    ) {
        super(errorDetail, cause, message);
    }

    public ResourceConflictFailure(
            List<ErrorDetail> errorDetail
    ) {
        super(errorDetail, null, "Resource conflict failure occurred.");
    }

}

