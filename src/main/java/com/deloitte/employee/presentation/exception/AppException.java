package com.deloitte.employee.presentation.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class AppException extends RuntimeException {
    private final ErrorResponse errorDetail;
    private final Throwable cause;
}
