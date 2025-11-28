package com.deloitte.employee.presentation.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequest {

    @Min(value = 0, message = "Page number must be 0 or greater")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Builder.Default
    private Integer size = 20;

    private List<SortRequest> sorts;
    private Map<String, String> search;

    @Builder(toBuilder = true)
    @Data
    public static class SortRequest {

        private String field;
        private String direction; // ASC/DESC
    }
}
