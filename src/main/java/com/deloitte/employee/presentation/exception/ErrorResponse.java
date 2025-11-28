package com.deloitte.employee.presentation.exception;

import com.deloitte.employee.domain.entities.ErrorDetail;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a standardized error response for API endpoints.
 * <p>
 * This DTO is used to send detailed information about errors to the client,
 * including a general message, an application-specific code, an associated
 * {@link ErrorCode}, and a list of detailed errors for fields or specific contexts.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * ErrorResponse response = ErrorResponse.builder()
 *     .message("Validation failed")
 *     .code(400)
 *     .errorCode(ErrorCode.BAD_REQUEST)
 *     .errorDetails(List.of(
 *         ErrorDetail.builder().field("email").message("Email is invalid").code("ERR_EMAIL").build(),
 *         ErrorDetail.builder().field("name").message("Name cannot be empty").code("ERR_NAME").build()
 *     ))
 *     .build();
 * }</pre>
 * </p>
 */
@Data
@Builder
@Schema(
        name = "ErrorResponse",
        description = """
                Standardized API error response structure used throughout the system.
                Contains an error message, numeric code, and optional list of detailed errors.
                """
)
public class ErrorResponse {

    /**
     * Human-readable summary of the error.
     */
    @Schema(
            example = "Validation failed",
            description = "Human-readable summary of the error"
    )
    private String message;

    /**
     * Numeric code corresponding to the error.
     * Can be used to map to HTTP status or custom application codes.
     */
    @Schema(
            example = "400",
            description = "Numeric code representing the error (can map to HTTP status or internal code)"
    )
    private int code;

    /**
     * Reference to the {@link ErrorCode} enum associated with this error.
     */
    @Schema(
            example = "ERR_SOMETHING: BAD_REQUEST",
            description = "High-level categorization of the error"
    )
    private ErrorCode errorCode;

    /**
     * Optional list of detailed errors providing more context.
     * Each {@link ErrorDetail} may describe a field-level error or other specifics.
     */
    @ArraySchema(
            arraySchema = @Schema(
                    description = "List of specific error details returned by the API"
            ),
            schema = @Schema(
                    implementation = ErrorDetail.class,
                    example = """
                            {
                              "field": "email",
                              "message": "Email format is invalid",
                              "code": "ERR_EMAIL"
                            }
                            """
            )
    )
    private List<ErrorDetail> errorDetails;
}
