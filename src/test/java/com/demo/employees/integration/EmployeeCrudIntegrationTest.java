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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Employee CRUD operations against a real MySQL database via Testcontainers.
 */
@DisplayName("Employee CRUD Integration Tests")
class EmployeeCrudIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/employees";

    private EmployeeRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = EmployeeRequest.builder()
                .firstName("Alberto")
                .middleName("Gabriel")
                .fatherName("Aguilera")
                .motherName("Valadez")
                .dateOfBirth("07-01-1955")
                .gender(Gender.MALE)
                .position("Singer")
                .build();
    }

    /**
     * Helper to create an employee and return the response entity.
     */
    private ResponseEntity<ApiResp<List<EmployeeResponse>>> createEmployee(EmployeeRequest request) {
        return adminTemplate().exchange(
                BASE_URL,
                HttpMethod.POST,
                new HttpEntity<>(List.of(request)),
                new ParameterizedTypeReference<>() {}
        );
    }

    /**
     * Helper to extract the first created employee's ID from a create response.
     */
    private UUID createAndGetId(EmployeeRequest request) {
        ResponseEntity<ApiResp<List<EmployeeResponse>>> response = createEmployee(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        List<EmployeeResponse> data = response.getBody().getData();
        assertThat(data).isNotEmpty();
        return data.get(0).getId();
    }

    @Nested
    @DisplayName("POST /api/v1/employees")
    class CreateEmployees {

        @Test
        @DisplayName("Should create a single employee and return 201 with generated UUID")
        void shouldCreateSingleEmployeeWithGeneratedUuid() {
            ResponseEntity<ApiResp<List<EmployeeResponse>>> response = createEmployee(validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ApiResp<List<EmployeeResponse>> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getStatus()).isEqualTo(201);
            assertThat(body.getMessage()).isEqualTo("Employees created successfully");

            List<EmployeeResponse> employees = body.getData();
            assertThat(employees).hasSize(1);

            EmployeeResponse created = employees.get(0);
            assertThat(created.getId()).isNotNull();
            assertThat(created.getFirstName()).isEqualTo("Alberto");
            assertThat(created.getFatherName()).isEqualTo("Aguilera");
            assertThat(created.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should create multiple employees in a single request")
        void shouldCreateMultipleEmployees() {
            EmployeeRequest second = EmployeeRequest.builder()
                    .firstName("Maria")
                    .fatherName("Rodriguez")
                    .motherName("Perez")
                    .dateOfBirth("20-03-1985")
                    .gender(Gender.FEMALE)
                    .position("Manager")
                    .build();

            ResponseEntity<ApiResp<List<EmployeeResponse>>> response = adminTemplate().exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(List.of(validRequest, second)),
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            List<EmployeeResponse> employees = response.getBody().getData();
            assertThat(employees).hasSize(2);
            assertThat(employees.get(0).getId()).isNotEqualTo(employees.get(1).getId());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees")
    class GetAllEmployees {

        @Test
        @DisplayName("Should return paginated list of active employees")
        void shouldReturnPaginatedListOfActiveEmployees() {
            createAndGetId(validRequest);

            ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> response = adminTemplate().exchange(
                    BASE_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResp<EmployeeResponse> page = response.getBody().getData();
            assertThat(page).isNotNull();
            assertThat(page.getContent()).isNotEmpty();
            assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/{id}")
    class GetEmployeeById {

        @Test
        @DisplayName("Should return employee when ID exists")
        void shouldReturnEmployeeWhenIdExists() {
            UUID id = createAndGetId(validRequest);

            ResponseEntity<ApiResp<EmployeeResponse>> response = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    id
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            EmployeeResponse employee = response.getBody().getData();
            assertThat(employee.getId()).isEqualTo(id);
            assertThat(employee.getFirstName()).isEqualTo("Alberto");
        }

        @Test
        @DisplayName("Should return 404 when employee ID does not exist")
        void shouldReturn404WhenEmployeeNotFound() {
            UUID nonExistentId = UUID.randomUUID();

            ResponseEntity<ApiResp<EmployeeResponse>> response = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    nonExistentId
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().getStatus()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/employees/{id}")
    class UpdateEmployee {

        @Test
        @DisplayName("Should fully replace employee record")
        void shouldFullyReplaceEmployeeRecord() {
            UUID id = createAndGetId(validRequest);

            EmployeeRequest updateRequest = EmployeeRequest.builder()
                    .firstName("Pedro")
                    .fatherName("Martinez")
                    .motherName("Sanchez")
                    .dateOfBirth("10-10-1988")
                    .gender(Gender.MALE)
                    .position("Senior Developer")
                    .build();

            ResponseEntity<ApiResp<EmployeeResponse>> response = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.PUT,
                    new HttpEntity<>(updateRequest),
                    new ParameterizedTypeReference<>() {},
                    id
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            EmployeeResponse updated = response.getBody().getData();
            assertThat(updated.getFirstName()).isEqualTo("Pedro");
            assertThat(updated.getFatherName()).isEqualTo("Martinez");
            assertThat(updated.getPosition()).isEqualTo("Senior Developer");
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/employees/{id}")
    class PatchEmployee {

        @Test
        @DisplayName("Should partially update only provided fields")
        void shouldPartiallyUpdateOnlyProvidedFields() {
            UUID id = createAndGetId(validRequest);

            EmployeePatchRequest patchRequest = EmployeePatchRequest.builder()
                    .position("Lead Developer")
                    .build();

            ResponseEntity<ApiResp<EmployeeResponse>> response = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.PATCH,
                    new HttpEntity<>(patchRequest),
                    new ParameterizedTypeReference<>() {},
                    id
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            EmployeeResponse patched = response.getBody().getData();
            assertThat(patched.getPosition()).isEqualTo("Lead Developer");
            assertThat(patched.getFirstName()).isEqualTo("Alberto");
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/employees/{id}")
    class DeleteEmployee {

        @Test
        @DisplayName("Should soft-delete employee by setting active to false")
        void shouldSoftDeleteEmployee() {
            UUID id = createAndGetId(validRequest);

            ResponseEntity<ApiResp<EmployeeResponse>> deleteResponse = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.DELETE,
                    null,
                    new ParameterizedTypeReference<>() {},
                    id
            );

            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(deleteResponse.getBody().getData().isActive()).isFalse();
        }

        @Test
        @DisplayName("Should exclude soft-deleted employee from default GET list")
        void shouldExcludeDeletedEmployeeFromDefaultGetList() {
            UUID id = createAndGetId(validRequest);

            // Delete the employee
            adminTemplate().exchange(
                    BASE_URL + "/{id}", HttpMethod.DELETE, null,
                    new ParameterizedTypeReference<ApiResp<EmployeeResponse>>() {}, id
            );

            // GET by ID without deleted=true should return 404
            ResponseEntity<ApiResp<EmployeeResponse>> getResponse = adminTemplate().exchange(
                    BASE_URL + "/{id}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    id
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should include soft-deleted employee when deleted=true")
        void shouldIncludeDeletedEmployeeWhenDeletedParamTrue() {
            UUID id = createAndGetId(validRequest);

            // Delete the employee
            adminTemplate().exchange(
                    BASE_URL + "/{id}", HttpMethod.DELETE, null,
                    new ParameterizedTypeReference<ApiResp<EmployeeResponse>>() {}, id
            );

            // GET by ID with deleted=true should return the employee
            ResponseEntity<ApiResp<EmployeeResponse>> getResponse = adminTemplate().exchange(
                    BASE_URL + "/{id}?deleted=true",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    id
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody().getData().isActive()).isFalse();
        }
    }
}