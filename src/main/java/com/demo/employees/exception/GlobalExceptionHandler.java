package com.demo.employees.exception;

import com.demo.employees.dto.response.ApiResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResp<Void>> handleValidation(MethodArgumentNotValidException ex) {
        logger.error("Validation failed: {}", ex.getMessage(), ex);

        List<ApiResp.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ApiResp.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ApiResp<Void> response = ApiResp.error(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles employee not found exceptions when a requested employee UUID does not exist.
     *
     * @param ex the not-found exception with a descriptive message
     * @return a 404 response with the exception message
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiResp<Void>> handleNotFound(EmployeeNotFoundException ex) {
        logger.error("Employee not found: {}", ex.getMessage(), ex);

        ApiResp<Void> response = ApiResp.error(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles search term validation failures when the search query is too short.
     *
     * @param ex the search-term-too-short exception with a descriptive message
     * @return a 400 response with the exception message
     */
    @ExceptionHandler(SearchTermTooShortException.class)
    public ResponseEntity<ApiResp<Void>> handleSearchTermTooShort(SearchTermTooShortException ex) {
        logger.error("Search term too short: {}", ex.getMessage(), ex);

        ApiResp<Void> response = ApiResp.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles constraint violation exceptions from {@code @Validated} on controller class level.
     * This catches validation errors for list elements in request bodies.
     *
     * @param ex the constraint violation exception
     * @return a 400 response with violation details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResp<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        logger.error("Constraint violation: {}", ex.getMessage(), ex);

        List<ApiResp.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    // Extract the leaf node name from the property path (e.g., "firstName" from "requests[0].firstName")
                    String fieldName = null;
                    for (javax.validation.Path.Node node : violation.getPropertyPath()) {
                        fieldName = node.getName();
                    }
                    return ApiResp.FieldError.builder()
                            .field(fieldName)
                            .message(violation.getMessage())
                            .build();
                })
                .collect(Collectors.toList());

        ApiResp<Void> response = ApiResp.error(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles type mismatch exceptions, typically caused by malformed UUID path variables.
     *
     * @param ex the type mismatch exception
     * @return a 400 response with a descriptive error message
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResp<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logger.error("Type mismatch: {}", ex.getMessage(), ex);

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ApiResp<Void> response = ApiResp.error(
                HttpStatus.BAD_REQUEST.value(),
                message,
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles unreadable HTTP message exceptions caused by malformed JSON or unreadable request bodies.
     *
     * @param ex the not-readable exception
     * @return a 400 response with a descriptive error message
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResp<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        logger.error("Malformed request body: {}", ex.getMessage(), ex);

        ApiResp<Void> response = ApiResp.error(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON or unreadable request body",
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Catch-all handler for any unexpected exceptions not covered by specific handlers.
     * Returns a generic error message without exposing internal details or stack traces.
     *
     * @param ex the unexpected exception
     * @return a 500 response with a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResp<Void>> handleGeneral(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ApiResp<Void> response = ApiResp.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
