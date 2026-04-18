package com.demo.employees.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Unified API response wrapper used by all endpoints for both success and error responses.
 *
 * @param <T> the type of the response data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResp<T> {

    private int status;
    private String message;
    private T data;
    private List<FieldError> errors;
    private LocalDateTime timestamp;


    /**
     * Nested class representing a single field-level validation error.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {

        private String field;
        private String message;
    }
    /**
     * Creates a success response with the given status, message, and data payload.
     * The timestamp is set to the current date-time automatically.
     *
     * @param status  the HTTP status code
     * @param message a human-readable success message
     * @param data    the response data payload
     * @param <T>     the type of the data payload
     * @return a fully populated success {@code ApiResponse}
     */

    public static <T> ApiResp<T> success(int status, String message, T data) {
        return ApiResp.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .errors(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with the given status, message, and field-level errors.
     * The timestamp is set to the current date-time automatically.
     *
     * @param status  the HTTP status code
     * @param message a human-readable error message
     * @param errors  the list of field-level validation errors
     * @return a fully populated error {@code ApiResponse}
     */

    public static ApiResp<Void> error(int status, String message, List<FieldError> errors) {
        return ApiResp.<Void>builder()
                .status(status)
                .message(message)
                .data(null)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

}