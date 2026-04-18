package com.demo.employees.integration;

import com.demo.employees.dto.request.EmployeePatchRequest;
import com.demo.employees.dto.request.EmployeeRequest;
import com.demo.employees.dto.response.ApiResp;
import com.demo.employees.dto.response.EmployeeResponse;
import com.demo.employees.dto.response.PaginatedResp;
import com.demo.employees.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying role-based access control and authentication enforcement.
 */
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/employees";

    private EmployeeRequest validRequest;
    private UUID existingEmployeeId;

    @BeforeEach
    void setUp() {
        validRequest = EmployeeRequest.builder()
                .firstName("Ana")
                .fatherName("Torres")
                .motherName("Vega")
                .dateOfBirth("15-05-1990")
                .gender(Gender.FEMALE)
                .position("Tester")
                .build();

        // Create an employee for tests that need an existing ID
        ResponseEntity<ApiResp<List<EmployeeResponse>>> createResponse = adminTemplate().exchange(
                BASE_URL,
                HttpMethod.POST,
                new HttpEntity<>(List.of(validRequest)),
                new ParameterizedTypeReference<>() {}
        );
        existingEmployeeId = createResponse.getBody().getData().get(0).getId();
    }

    @Nested
    @DisplayName("ADMIN role access")
    class AdminAccess {

        @Test
        @DisplayName("ADMIN should access GET list endpoint")
        void adminShouldAccessGetListEndpoint() {
            ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> response = adminTemplate().exchange(
                    BASE_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("ADMIN should access GET by ID endpoint")
        void adminShouldAccessGetByIdEndpoint() {
            ResponseEntity<ApiResp<EmployeeResponse>> response = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    existingEmployeeId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("ADMIN should access POST endpoint")
        void adminShouldAccessPostEndpoint() {
            EmployeeRequest newRequest = EmployeeRequest.builder()
                    .firstName("Luis")
                    .fatherName("Morales")
                    .motherName("Rios")
                    .dateOfBirth("01-01-1992")
                    .gender(Gender.MALE)
                    .position("Designer")
                    .build();

            ResponseEntity<ApiResp<List<EmployeeResponse>>> response = adminTemplate().exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(List.of(newRequest)),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("ADMIN should access PUT endpoint")
        void adminShouldAccessPutEndpoint() {
            EmployeeRequest updateRequest = EmployeeRequest.builder()
                    .firstName("Updated")
                    .fatherName("Torres")
                    .motherName("Vega")
                    .dateOfBirth("15-05-1990")
                    .gender(Gender.FEMALE)
                    .position("Senior Tester")
                    .build();

            ResponseEntity<ApiResp<EmployeeResponse>> response = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.PUT,
                    new HttpEntity<>(updateRequest),
                    new ParameterizedTypeReference<>() {},
                    existingEmployeeId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("ADMIN should access PATCH endpoint")
        void adminShouldAccessPatchEndpoint() {
            EmployeePatchRequest patchRequest = EmployeePatchRequest.builder()
                    .position("Lead Tester")
                    .build();

            ResponseEntity<ApiResp<EmployeeResponse>> response = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.PATCH,
                    new HttpEntity<>(patchRequest),
                    new ParameterizedTypeReference<>() {},
                    existingEmployeeId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("ADMIN should access DELETE endpoint")
        void adminShouldAccessDeleteEndpoint() {
            ResponseEntity<ApiResp<EmployeeResponse>> response = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.DELETE,
                    null,
                    new ParameterizedTypeReference<>() {},
                    existingEmployeeId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("USER role access")
    class UserAccess {

        @Test
        @DisplayName("USER should access GET list endpoint")
        void userShouldAccessGetListEndpoint() {
            ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> response = userTemplate().exchange(
                    BASE_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("USER should access GET by ID endpoint")
        void userShouldAccessGetByIdEndpoint() {
            ResponseEntity<ApiResp<EmployeeResponse>> response = userTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    existingEmployeeId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("USER should receive 403 on POST endpoint")
        void userShouldReceive403OnPostEndpoint() {
            ResponseEntity<ApiResp<Void>> response = userTemplate().exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(List.of(validRequest)),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().getStatus()).isEqualTo(403);
        }

        @Test
        @DisplayName("USER should receive 403 on PUT endpoint")
        void userShouldReceive403OnPutEndpoint() {
            ResponseEntity<ApiResp<Void>> response = userTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.PUT,
                    new HttpEntity<>(validRequest),
                    new ParameterizedTypeReference<>() {},
                    existingEmployeeId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().getStatus()).isEqualTo(403);
        }

        @Test
        @DisplayName("USER should receive 403 on PATCH endpoint")
        void userShouldReceive403OnPatchEndpoint() {
            EmployeePatchRequest patchRequest = EmployeePatchRequest.builder()
                    .position("Hacker")
                    .build();

            ResponseEntity<ApiResp<Void>> response = userTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.PATCH,
                    new HttpEntity<>(patchRequest),
                    new ParameterizedTypeReference<>() {},
                    existingEmployeeId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().getStatus()).isEqualTo(403);
        }

        @Test
        @DisplayName("USER should receive 403 on DELETE endpoint")
        void userShouldReceive403OnDeleteEndpoint() {
            ResponseEntity<ApiResp<Void>> response = userTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.DELETE,
                    null,
                    new ParameterizedTypeReference<>() {},
                    existingEmployeeId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().getStatus()).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("Unauthenticated access")
    class UnauthenticatedAccess {

        @Test
        @DisplayName("Unauthenticated request should receive 401")
        void unauthenticatedRequestShouldReceive401() {
            ResponseEntity<ApiResp<Void>> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(401);
            assertThat(response.getBody().getMessage()).isNotBlank();
        }

        @Test
        @DisplayName("Unauthenticated POST request should receive 401")
        void unauthenticatedPostRequestShouldReceive401() {
            ResponseEntity<ApiResp<Void>> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(List.of(validRequest)),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody().getStatus()).isEqualTo(401);
        }
    }
}
