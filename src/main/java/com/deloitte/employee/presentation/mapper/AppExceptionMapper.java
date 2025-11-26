package com.deloitte.employee.presentation.mapper;

import com.deloitte.employee.domain.failure.*;
import com.deloitte.employee.domain.mapper.ExceptionMapper;
import com.deloitte.employee.presentation.exception.AppException;
import com.deloitte.employee.presentation.exception.ErrorCode;
import com.deloitte.employee.presentation.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
class AppExceptionMapper implements ExceptionMapper<AppException> {

    @Override
    public AppException map(OperationFailure failure) {

        ErrorResponse response = switch (failure) {

            case ValidationFailure f -> ErrorResponse.builder()
                    .message(f.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value())
                    .errorCode(ErrorCode.BAD_REQUEST)
                    .errorDetails(f.getErrorDetail())
                    .build();

            case ResourceNotFoundFailure f -> ErrorResponse.builder()
                    .message(f.getMessage())
                    .code(HttpStatus.NOT_FOUND.value())
                    .errorCode(ErrorCode.RESOURCE_NOT_FOUND)
                    .errorDetails(f.getErrorDetail())
                    .build();

            case ResourceConflictFailure f -> ErrorResponse.builder()
                    .message(f.getMessage())
                    .code(HttpStatus.CONFLICT.value())
                    .errorCode(ErrorCode.RESOURCE_CONFLICT)
                    .errorDetails(f.getErrorDetail())
                    .build();

            case InfraStructureFailure f -> ErrorResponse.builder()
                    .message("Service temporarily unavailable")
                    .code(HttpStatus.SERVICE_UNAVAILABLE.value())
                    .errorCode(ErrorCode.SERVICE_UNAVAILABLE)
                    .errorDetails(f.getErrorDetail())
                    .build();

            case SystemFailure f -> ErrorResponse.builder()
                    .message("Internal server error")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                    .errorDetails(f.getErrorDetail())
                    .build();
        };

        return new AppException(response, failure.getCause());
    }
}
