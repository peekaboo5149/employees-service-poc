package com.deloitte.employee.presentation.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum representing standardized error codes for the User Service.
 * Each error code includes:
 * - a unique code
 * - a default message
 * - an associated HTTP status
 */
@Getter
public enum ErrorCode {

    RESOURCE_NOT_FOUND("ERR_101", "Resource not found", HttpStatus.NOT_FOUND),
    BAD_REQUEST("ERR_102", "Bad request", HttpStatus.BAD_REQUEST),
    RESOURCE_CONFLICT("ERR_103", "Resource conflict", HttpStatus.CONFLICT),
    SERVICE_UNAVAILABLE("ERR_105", "Service unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR("ERR_104", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    /**
     * Returns a formatted string with code and message.
     */
    @Override
    public String toString() {
        return String.format("%s: %s", code, message);
    }
}
