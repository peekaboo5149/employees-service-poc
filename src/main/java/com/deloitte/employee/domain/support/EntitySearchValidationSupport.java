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

    default Either<ValidationFailure, Search> validateSearch(
            Search search,
            Class<? extends T> entityClass
    ) {
        // 1️⃣ If no search provided → return empty
        if (search == null) {
            return Either.right(Search.empty());
        }

        // 2️⃣ If empty → return it
        if (!search.hasCriteria()) {
            return Either.right(search);
        }

        // 3️⃣ Validate all criteria against entity fields
        List<ErrorDetail> errors = new ArrayList<>();
        Map<String, String> criteria = search.getCriteria();

        // Build a set of valid field names once
        var validFields = new java.util.HashSet<String>();
        for (Field field : entityClass.getDeclaredFields()) {
            validFields.add(field.getName());
        }

        // Validate criteria keys
        for (String fieldName : criteria.keySet()) {
            if (!validFields.contains(fieldName)) {
                errors.add(
                        ErrorDetail.builder()
                                .field(fieldName)
                                .message("Invalid search field")
                                .code("ERR_INVALID_SEARCH_FIELD")
                                .build()
                );
            }
        }

        // 4️⃣ If errors → fail
        if (!errors.isEmpty()) {
            return Either.left(new ValidationFailure(errors));
        }

        // 5️⃣ Return a defensive copy
        return Either.right(Search.copyOf(search));
    }
}
