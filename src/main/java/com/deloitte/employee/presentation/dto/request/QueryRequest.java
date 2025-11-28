package com.deloitte.employee.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Schema(name = "QueryRequest", description = "Paginated query request")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequest {

    @Schema(example = "0", description = "Page number (0-indexed)")
    @Min(value = 0, message = "Page number must be 0 or greater")
    @Builder.Default
    private Integer page = 0;

    @Schema(example = "20", description = "Page size")
    @Min(value = 1, message = "Page size must be at least 1")
    @Builder.Default
    private Integer size = 20;

    private List<SortRequest> sorts;
    private Map<String, String> search;

    @Schema(name = "SortRequest", description = "Sort field and direction")
    @Builder(toBuilder = true)
    @Data
    public static class SortRequest {

        @Schema(example = "fullName")
        private String field;
        @Schema(example = "ASC")
        private String direction; // ASC/DESC
    }
}
