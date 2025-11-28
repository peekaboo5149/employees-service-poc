package com.deloitte.employee.presentation.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Schema(
        name = "AppException",
        description = "Application-level exception wrapper that carries an ErrorResponse payload"
)
public class AppException extends RuntimeException {

    private final ErrorResponse errorDetail;

    public AppException(ErrorResponse errorDetail, Throwable cause) {
        super(errorDetail != null ? errorDetail.getMessage() : null, cause);
        this.errorDetail = errorDetail;
    }

    public static AppException of(ErrorResponse errorDetail) {
        return new AppException(errorDetail, null);
    }
}
