package com.deloitte.employee.presentation.exception;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a single error detail for a specific field or context.
 */
@Data
@Builder
public class ErrorDetail {

    /**
     * The name of the field or context related to this error.
     * Can be null if not field-specific.
     */
    private String field;

    /**
     * Human-readable error message for this specific detail.
     */
    private String message;

    /**
     * Optional additional information or code for this detail.
     */
    private String code;
}
