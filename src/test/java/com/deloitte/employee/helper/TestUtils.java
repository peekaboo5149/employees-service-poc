package com.deloitte.employee.helper;

import com.deloitte.employee.domain.mapper.ExceptionMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class TestUtils {
    @SuppressWarnings("unchecked")
    public static <T extends RuntimeException> ExceptionMapper<T> mockExceptionMapper() {
        return mock(ExceptionMapper.class);
    }

    /**
     * Generate `count` lexicographically ordered full names.
     * Examples: "Test A", "Test B", ... "Test Z", "Test AA", "Test AB", ...
     */
    public static String[] getLexicographicalFullNames(int count) {
        if (count <= 0) {
            return new String[0];
        }
        List<String> names = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String lastName = toBase26(i);
            names.add("Test " + lastName);
        }
        return names.toArray(new String[0]);
    }

    /**
     * Convert a zero-based index to letters in "A..Z, AA..ZZ, AAA..." style.
     * 0 -> "A", 1 -> "B", ..., 25 -> "Z", 26 -> "AA", 27 -> "AB", ...
     */
    private static String toBase26(int index) {
        if (index < 0) throw new IllegalArgumentException("index must be >= 0");
        StringBuilder sb = new StringBuilder();
        int n = index;
        while (n >= 0) {
            int rem = n % 26;
            sb.append((char) ('A' + rem));
            n = (n / 26) - 1;
        }
        return sb.reverse().toString();
    }

}
