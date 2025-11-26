package com.deloitte.employee.presentation.mapper;

import com.deloitte.employee.domain.failure.OperationFailure;
import com.deloitte.employee.domain.mapper.ExceptionMapper;
import com.deloitte.employee.presentation.exception.AppException;
import org.springframework.stereotype.Component;

@Component
class AppExceptionMapper implements ExceptionMapper<AppException> {

    @Override
    public AppException map(OperationFailure failure) {
        // TODO
        throw new RuntimeException("Not implemented yet");
    }


}
