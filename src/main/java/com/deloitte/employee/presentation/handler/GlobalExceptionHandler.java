package com.deloitte.employee.presentation.handler;


import com.deloitte.employee.presentation.exception.AppException;
import com.deloitte.employee.presentation.exception.ErrorCode;
import com.deloitte.employee.presentation.exception.ErrorDetail;
import com.deloitte.employee.presentation.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler to intercept exceptions across all controllers.
 * Handles custom AppException, validation errors, and general exceptions.
 */
@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    /**
     * Handles AppException and converts it to a ResponseEntity with proper status and body.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        ErrorResponse errorResponse = ex.getErrorDetail();

        int statusCode = errorResponse.getErrorCode() != null
            ? errorResponse.getErrorCode().getHttpStatus().value()
            : 500;

        return ResponseEntity.status(statusCode)
            .body(errorResponse);
    }

    /**
     * Handles Spring validation exceptions and maps them to ErrorResponse with ErrorDetail.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        List<ErrorDetail> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> ErrorDetail.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .code("VALIDATION_ERROR")
                .build())
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message("Validation failed for request")
            .code(HttpStatus.BAD_REQUEST.value())
            .errorCode(ErrorCode.BAD_REQUEST)
            .errorDetails(details)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * Handles all other uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message("An unexpected error occurred")
            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
            .errorDetails(List.of(
                ErrorDetail.builder()
                    .message(ex.getMessage())
                    .build()
            ))
            .build();
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }

    /**
     * Handles exceptions of type {@link MethodArgumentTypeMismatchException}.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String fieldName = ex.getName();
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";

        ErrorResponse error = ErrorResponse.builder()
            .code(HttpStatus.BAD_REQUEST.value())
            .errorCode(ErrorCode.BAD_REQUEST)
            .message("Invalid path variable type")
            .errorDetails(List.of(
                ErrorDetail.builder()
                    .code("ERR_INVALID_PATH_VARIABLE")
                    .field(fieldName)
                    .message("Invalid value: " + invalidValue + ". Expected a number.")
                    .build()
            ))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
