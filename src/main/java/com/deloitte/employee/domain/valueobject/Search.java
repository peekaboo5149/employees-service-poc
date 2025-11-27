package com.deloitte.employee.domain.valueobject;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public final class Search {

    private final Map<String, String> criteria;

    private Search(Map<String, String> criteria) {
        this.criteria = criteria;
    }

    /** Create an empty Search */
    public static Search empty() {
        return new Search(new HashMap<>());
    }


    /** Create a copy of an existing Search */
    public static Search copyOf(Search other) {
        return new Search(new HashMap<>(other.criteria));
    }

    /** Add a new search criterion */
    public void add(String field, String value) {
        if (value != null && !value.isBlank()) {
            criteria.put(field, value);
        }
    }

    public boolean hasCriteria() {
        return !criteria.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Search other)) return false;
        return Objects.equals(criteria, other.criteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteria);
    }
}
