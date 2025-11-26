package com.deloitte.employee.domain.failure;

import com.deloitte.employee.domain.entities.ErrorDetail;

import java.util.List;

public final class InfraStructureFailure extends OperationFailure {

    public InfraStructureFailure(List<ErrorDetail> errorDetail, Throwable cause, String message) {
        super(errorDetail, cause, message);
    }

    public InfraStructureFailure(List<ErrorDetail> errorDetail) {
        super(errorDetail, null, "Infrastructure failure occurred.");
    }
}
