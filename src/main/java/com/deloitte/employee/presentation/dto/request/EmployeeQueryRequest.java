package com.deloitte.employee.presentation.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EmployeeQueryRequest {

    private Integer page = 0;          // default page
    private Integer size = 20;         // default size

    private List<SortRequest> sorts;   // optional sorting
    private Map<String, String> search; // optional search fields

    @Data
    public static class SortRequest {
        private String field;      // FULL_NAME, EMAIL, etc
        private String direction;  // ASC/DESC
    }
}