package com.deloitte.employee.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class QueryRequest {

    private Integer page = 0;          // default page
    private Integer size = 20;         // default size

    private List<SortRequest> sorts;   // optional sorting
    private Map<String, String> search; // optional search fields

    @Builder(toBuilder = true)
    @Data
    public static class SortRequest {
        private String field;      // FULL_NAME, EMAIL, etc
        private String direction;  // ASC/DESC
    }
}