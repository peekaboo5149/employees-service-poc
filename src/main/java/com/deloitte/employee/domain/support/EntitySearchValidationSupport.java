package com.deloitte.employee.domain.support;

import com.deloitte.employee.domain.entities.ErrorDetail;
import com.deloitte.employee.domain.failure.ValidationFailure;
import com.deloitte.employee.domain.valueobject.Search;
import io.vavr.control.Either;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface EntitySearchValidationSupport<T> {
    default Either<ValidationFailure, Search> validateSearch(Search search, Class<? extends T> entityClass) {
        if (search == null || !search.hasCriteria()) {
            return Either.right(search != null ? search : Search.NEW);
        }
        List<ErrorDetail> errors = new ArrayList<>();
        Map<String, String> criteria = search.getCriteria();

        for (String fieldName : criteria.keySet()) {
            boolean fieldExists = false;
            for (Field field : entityClass.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    fieldExists = true;
                    break;
                }
            }
            if (!fieldExists) {
                errors.add(ErrorDetail.builder()
                        .field(fieldName)
                        .message("Invalid search field")
                        .code("ERR_INVALID_SEARCH_FIELD")
                        .build());
            }
        }

        if (!errors.isEmpty()) {
            return Either.left(new ValidationFailure(errors));
        }

        return Either.right(search);
    }
}
