package com.deloitte.employee.domain.failure;

import com.deloitte.employee.domain.entities.ErrorDetail;
import lombok.Getter;

import java.util.List;

@Getter
public sealed abstract class OperationFailure extends RuntimeException
        permits InfraStructureFailure, ResourceConflictFailure, ResourceNotFoundFailure, SystemFailure, ValidationFailure {

    private final List<ErrorDetail> errorDetail;

    protected OperationFailure(
            List<ErrorDetail> errorDetail,
            Throwable cause,
            String message
    ) {
        super(message, cause);
        this.errorDetail = errorDetail;
    }

}


