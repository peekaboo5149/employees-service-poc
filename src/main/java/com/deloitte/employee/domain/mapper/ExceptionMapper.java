package com.deloitte.employee.domain.mapper;

import com.deloitte.employee.domain.failure.OperationFailure;

public interface ExceptionMapper<E extends RuntimeException> {
    E map(OperationFailure failure);

    default <T> T mapAndThrow(OperationFailure failure) {
        throw map(failure);
    }
}
