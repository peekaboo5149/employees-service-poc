package com.deloitte.employee.domain.valueobject;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Search {

    private Search(){}

    public static final Search NEW = new Search();

    private final Map<String, String> criteria = new HashMap<>();

    public void add(String field, String value) {
        if (value != null && !value.isBlank()) {
            criteria.put(field, value);
        }
    }

    public boolean hasCriteria() {
        return !criteria.isEmpty();
    }
}
