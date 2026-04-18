package com.demo.employees.integration;

import com.demo.employees.dto.response.ApiResp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying input validation rules against a real MySQL database.
 */
@DisplayName("Validation Integration Tests")
class ValidationIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/employees";

    /**
     * Helper to POST a raw JSON string as admin and return the response.
     */
    private ResponseEntity<ApiResp<Void>> postRawJson(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        return adminTemplate().exchange(
                BASE_URL,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );
    }

    @Nested
    @DisplayName("Missing required fields")
    class MissingRequiredFields {

        @Test
        @DisplayName("Should return 400 when firstName is missing")
        void shouldReturn400WhenFirstNameMissing() {
            String json = "[{\"fatherName\":\"Garcia\",\"motherName\":\"Lopez\","
                    + "\"dateOfBirth\":\"15-05-1990\",\"gender\":\"MALE\",\"position\":\"Developer\"}]";

            ResponseEntity<ApiResp<Void>> response = postRawJson(json);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getErrors()).isNotEmpty();
            assertThat(response.getBody().getErrors())
                    .anyMatch(e -> "firstName".equals(e.getField()));
        }

        @Test
        @DisplayName("Should return 400 when multiple required fields are missing")
        void shouldReturn400WhenMultipleRequiredFieldsMissing() {
            String json = "[{\"middleName\":\"Carlos\"}]";

            ResponseEntity<ApiResp<Void>> response = postRawJson(json);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getErrors()).isNotNull();
            assertThat(response.getBody().getErrors().size()).isGreaterThanOrEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Date of birth validation")
    class DateOfBirthValidation {

        @Test
        @DisplayName("Should return 400 when employee is under 18")
        void shouldReturn400WhenEmployeeUnder18() {
            // Use a date that makes the person under 18
            String json = "[{\"firstName\":\"Young\",\"fatherName\":\"Person\","
                    + "\"motherName\":\"Test\",\"dateOfBirth\":\"15-05-2015\","
                    + "\"gender\":\"MALE\",\"position\":\"Intern\"}]";

            ResponseEntity<ApiResp<Void>> response = postRawJson(json);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getStatus()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Name length validation")
    class NameLengthValidation {

        @Test
        @DisplayName("Should return 400 when first name exceeds 50 characters")
        void shouldReturn400WhenFirstNameExceeds50Characters() {
            String longName = "A".repeat(51);
            String json = "[{\"firstName\":\"" + longName + "\",\"fatherName\":\"Garcia\","
                    + "\"motherName\":\"Lopez\",\"dateOfBirth\":\"15-05-1990\","
                    + "\"gender\":\"MALE\",\"position\":\"Developer\"}]";

            ResponseEntity<ApiResp<Void>> response = postRawJson(json);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getErrors()).isNotEmpty();
            assertThat(response.getBody().getErrors())
                    .anyMatch(e -> "firstName".equals(e.getField()));
        }
    }

    @Nested
    @DisplayName("Gender enum validation")
    class GenderEnumValidation {

        @Test
        @DisplayName("Should return 400 when gender value is invalid")
        void shouldReturn400WhenGenderValueIsInvalid() {
            String json = "[{\"firstName\":\"Juan\",\"fatherName\":\"Garcia\","
                    + "\"motherName\":\"Lopez\",\"dateOfBirth\":\"15-05-1990\","
                    + "\"gender\":\"INVALID_GENDER\",\"position\":\"Developer\"}]";

            ResponseEntity<ApiResp<Void>> response = postRawJson(json);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getStatus()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Malformed UUID validation")
    class MalformedUuidValidation {

        @Test
        @DisplayName("Should return 400 when UUID is malformed")
        void shouldReturn400WhenUuidIsMalformed() {
            ResponseEntity<ApiResp<Void>> response = adminTemplate().exchange(
                    BASE_URL + "/not-a-valid-uuid",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getStatus()).isEqualTo(400);
        }
    }
}
