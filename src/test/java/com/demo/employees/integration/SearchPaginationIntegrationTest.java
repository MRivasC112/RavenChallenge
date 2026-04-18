package com.demo.employees.integration;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for search and pagination functionality against a real MySQL database.
 */
@DisplayName("Search and Pagination Integration Tests")
class SearchPaginationIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/employees";
    private static final String SEARCH_URL = BASE_URL + "/search";

    @BeforeEach
    void setUp() {
        // Seed test data for search and pagination tests
        List<EmployeeRequest> employees = List.of(
                EmployeeRequest.builder()
                        .firstName("Alejandro")
                        .fatherName("Fernandez")
                        .motherName("Gomez")
                        .dateOfBirth("15-05-1990")
                        .gender(Gender.MALE)
                        .position("Developer")
                        .build(),
                EmployeeRequest.builder()
                        .firstName("Alejandra")
                        .fatherName("Martinez")
                        .motherName("Ruiz")
                        .dateOfBirth("20-03-1985")
                        .gender(Gender.FEMALE)
                        .position("Manager")
                        .build(),
                EmployeeRequest.builder()
                        .firstName("Roberto")
                        .fatherName("Hernandez")
                        .motherName("Diaz")
                        .dateOfBirth("10-10-1988")
                        .gender(Gender.MALE)
                        .position("Analyst")
                        .build()
        );

        adminTemplate().exchange(
                BASE_URL,
                HttpMethod.POST,
                new HttpEntity<>(employees),
                new ParameterizedTypeReference<ApiResp<List<EmployeeResponse>>>() {}
        );
    }

    @Nested
    @DisplayName("Search by name")
    class SearchByName {

        @Test
        @DisplayName("Should return matching employees for valid search term")
        void shouldReturnMatchingEmployeesForValidSearchTerm() {
            ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> response = adminTemplate().exchange(
                    SEARCH_URL + "?name=Alejandr",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResp<EmployeeResponse> page = response.getBody().getData();
            assertThat(page.getContent()).isNotEmpty();
            page.getContent().forEach(emp ->
                    assertThat(emp.getFirstName().toLowerCase() +
                            emp.getFatherName().toLowerCase() +
                            emp.getMotherName().toLowerCase())
                            .containsIgnoringCase("alejandr")
            );
        }

        @Test
        @DisplayName("Should return 400 when search term is too short")
        void shouldReturn400WhenSearchTermTooShort() {
            ResponseEntity<ApiResp<Void>> response = adminTemplate().exchange(
                    SEARCH_URL + "?name=ab",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getStatus()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Pagination")
    class Pagination {

        @Test
        @DisplayName("Should respect page and size parameters on list endpoint")
        void shouldRespectPageAndSizeParametersOnListEndpoint() {
            ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> response = adminTemplate().exchange(
                    BASE_URL + "?page=0&size=2",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResp<EmployeeResponse> page = response.getBody().getData();
            assertThat(page.getSize()).isEqualTo(2);
            assertThat(page.getPage()).isEqualTo(0);
            assertThat(page.getContent().size()).isLessThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should respect page and size parameters on search endpoint")
        void shouldRespectPageAndSizeParametersOnSearchEndpoint() {
            ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> response = adminTemplate().exchange(
                    SEARCH_URL + "?name=Alejandr&page=0&size=1",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResp<EmployeeResponse> page = response.getBody().getData();
            assertThat(page.getSize()).isEqualTo(1);
            assertThat(page.getContent().size()).isLessThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should support sort parameter on list endpoint")
        void shouldSupportSortParameterOnListEndpoint() {
            ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> response = adminTemplate().exchange(
                    BASE_URL + "?sort=firstName,asc",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResp<EmployeeResponse> page = response.getBody().getData();
            assertThat(page.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("Should cap page size at 100")
        void shouldCapPageSizeAt100() {
            ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> response = adminTemplate().exchange(
                    BASE_URL + "?size=200",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            PaginatedResp<EmployeeResponse> page = response.getBody().getData();
            assertThat(page.getSize()).isLessThanOrEqualTo(100);
        }
    }
}
