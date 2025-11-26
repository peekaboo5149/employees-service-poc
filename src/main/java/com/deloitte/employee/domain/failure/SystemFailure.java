package com.deloitte.employee.domain.failure;

import com.deloitte.employee.domain.entities.ErrorDetail;

import java.util.List;

public final class SystemFailure extends OperationFailure {
    public SystemFailure(List<ErrorDetail> errorDetail, Throwable cause, String message) {
        super(errorDetail, cause, message);
    }

    public SystemFailure(List<ErrorDetail> errorDetail) {
        super(errorDetail, null, "System failure occurred.");
    }
}
