package com.deloitte.employee.helper;

import com.deloitte.employee.domain.mapper.ExceptionMapper;

import static org.mockito.Mockito.mock;

public class TestUtils {
    @SuppressWarnings("unchecked")
    public static <T extends RuntimeException> ExceptionMapper<T> mockExceptionMapper() {
        return mock(ExceptionMapper.class);
    }
}
