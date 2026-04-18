package com.demo.employees.exception;

import com.demo.employees.dto.response.ApiResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link GlobalExceptionHandler} verifying each exception type
 * maps to the correct HTTP status and ApiResponse structure.
 */
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleValidation should return 400 with field-level errors from MethodArgumentNotValidException")
    void handleValidationShouldReturn400WithFieldErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "employeeRequest");
        bindingResult.addError(new FieldError("employeeRequest", "firstName", "First name is required"));
        bindingResult.addError(new FieldError("employeeRequest", "position", "Position is required"));

        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ApiResp<Void>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getErrors()).hasSize(2);
        assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("firstName");
        assertThat(response.getBody().getErrors().get(0).getMessage()).isEqualTo("First name is required");
        assertThat(response.getBody().getErrors().get(1).getField()).isEqualTo("position");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    @DisplayName("handleNotFound should return 404 with descriptive message from EmployeeNotFoundException")
    void handleNotFoundShouldReturn404WithDescriptiveMessage() {
        UUID employeeId = UUID.randomUUID();
        EmployeeNotFoundException ex = new EmployeeNotFoundException(employeeId);

        ResponseEntity<ApiResp<Void>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Employee not found with id: " + employeeId);
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    @DisplayName("handleSearchTermTooShort should return 400 with message from SearchTermTooShortException")
    void handleSearchTermTooShortShouldReturn400WithMessage() {
        SearchTermTooShortException ex = new SearchTermTooShortException(3);

        ResponseEntity<ApiResp<Void>> response = handler.handleSearchTermTooShort(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Search term must be at least 3 characters");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    @DisplayName("handleTypeMismatch should return 400 with descriptive message from MethodArgumentTypeMismatchException")
    void handleTypeMismatchShouldReturn400WithDescriptiveMessage() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "not-a-uuid", UUID.class, "id", null, new IllegalArgumentException("Invalid UUID"));

        ResponseEntity<ApiResp<Void>> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("not-a-uuid");
        assertThat(response.getBody().getMessage()).contains("id");
        assertThat(response.getBody().getMessage()).contains("UUID");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    @DisplayName("handleUnreadable should return 400 with message from HttpMessageNotReadableException")
    void handleUnreadableShouldReturn400WithMessage() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Malformed JSON", mock(HttpInputMessage.class));

        ResponseEntity<ApiResp<Void>> response = handler.handleUnreadable(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Malformed JSON or unreadable request body");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    @DisplayName("handleGeneral should return 500 with generic message and no stack trace")
    void handleGeneralShouldReturn500WithGenericMessage() {
        Exception ex = new RuntimeException("Something broke internally");

        ResponseEntity<ApiResp<Void>> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        // Verify no stack trace or internal details leak into the response
        assertThat(response.getBody().getMessage()).doesNotContain("Something broke internally");
        assertThat(response.getBody().getData()).isNull();
    }

    /**
     * Dummy method used to create a MethodParameter for MethodArgumentNotValidException construction.
     */
    @SuppressWarnings("unused")
    private void dummyMethod(String param) {
        // no-op — exists only to provide a MethodParameter for test setup
    }
}
