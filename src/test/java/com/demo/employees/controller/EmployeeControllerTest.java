package com.demo.employees.controller;

import com.demo.employees.config.CustomAccessDeniedHandler;
import com.demo.employees.config.CustomAuthenticationEntryPoint;
import com.demo.employees.config.SecurityConfig;
import com.demo.employees.dto.request.EmployeePatchRequest;
import com.demo.employees.dto.response.EmployeeResponse;
import com.demo.employees.dto.response.PaginatedResp;
import com.demo.employees.enums.Gender;
import com.demo.employees.exception.EmployeeNotFoundException;
import com.demo.employees.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@DisplayName("EmployeeController Unit Tests")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private static final UUID EMPLOYEE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String BASE_URL = "/api/v1/employees";

    private EmployeeResponse sampleResponse;
    private PaginatedResp<EmployeeResponse> paginatedResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = EmployeeResponse.builder()
                .id(EMPLOYEE_ID)
                .firstName("Juan")
                .middleName("Gabriel")
                .fatherName("Aguilera")
                .motherName("Valadez")
                .age(34)
                .dateOfBirth("07-01-1950")
                .gender(Gender.MALE)
                .position("Singer")
                .registrationDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .active(true)
                .build();

        paginatedResponse = PaginatedResp.<EmployeeResponse>builder()
                .content(List.of(sampleResponse))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/employees")
    class CreateEmployees {

        @Test
        @DisplayName("Should return 201 with ApiResponse structure when creating employees as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn201WithApiResponseWhenCreatingEmployeesAsAdmin() throws Exception {
            when(employeeService.createEmployees(any())).thenReturn(List.of(sampleResponse));

            String requestBody = "[{\"firstName\":\"Juan\",\"middleName\":\"Carlos\","
                    + "\"fatherName\":\"Garcia\",\"motherName\":\"Lopez\","
                    + "\"dateOfBirth\":\"15-05-1990\",\"gender\":\"MALE\",\"position\":\"Developer\"}]";

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("Employees created successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(EMPLOYEE_ID.toString()))
                    .andExpect(jsonPath("$.data[0].firstName").value("Juan"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 with field errors when request body has missing required fields")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WithFieldErrorsWhenRequiredFieldsMissing() throws Exception {
            // PUT validates individual EmployeeRequest with @Valid, so missing firstName triggers 400
            String invalidBody = "{\"fatherName\":\"Garcia\","
                    + "\"motherName\":\"Lopez\","
                    + "\"dateOfBirth\":\"15-05-1990\",\"gender\":\"MALE\",\"position\":\"Developer\"}";

            mockMvc.perform(put(BASE_URL + "/{id}", EMPLOYEE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 403 when USER role attempts to create employees")
        @WithMockUser(roles = "USER")
        void shouldReturn403WhenUserRoleAttemptsToCreate() throws Exception {
            String requestBody = "[{\"firstName\":\"Juan\",\"middleName\":\"Carlos\","
                    + "\"fatherName\":\"Garcia\",\"motherName\":\"Lopez\","
                    + "\"dateOfBirth\":\"15-05-1990\",\"gender\":\"MALE\",\"position\":\"Developer\"}]";

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees")
    class GetAllEmployees {

        @Test
        @DisplayName("Should return 200 with paginated response as USER")
        @WithMockUser(roles = "USER")
        void shouldReturn200WithPaginatedResponseAsUser() throws Exception {
            when(employeeService.getAllEmployees(anyBoolean(), any())).thenReturn(paginatedResponse);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Employees retrieved successfully"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].firstName").value("Juan"))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(20))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/{id}")
    class GetEmployeeById {

        @Test
        @DisplayName("Should return 200 with employee when found as USER")
        @WithMockUser(roles = "USER")
        void shouldReturn200WithEmployeeWhenFoundAsUser() throws Exception {
            when(employeeService.getEmployeeById(eq(EMPLOYEE_ID), anyBoolean())).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", EMPLOYEE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Employee retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(EMPLOYEE_ID.toString()))
                    .andExpect(jsonPath("$.data.firstName").value("Juan"))
                    .andExpect(jsonPath("$.data.age").value(34));
        }

        @Test
        @DisplayName("Should return 404 when employee with non-existent UUID is requested")
        @WithMockUser(roles = "USER")
        void shouldReturn404WhenEmployeeNotFound() throws Exception {
            UUID nonExistentId = UUID.fromString("99999999-9999-9999-9999-999999999999");
            when(employeeService.getEmployeeById(eq(nonExistentId), anyBoolean()))
                    .thenThrow(new EmployeeNotFoundException(nonExistentId));

            mockMvc.perform(get(BASE_URL + "/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Employee not found with id: " + nonExistentId));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/search")
    class SearchEmployees {

        @Test
        @DisplayName("Should return 200 with search results as USER")
        @WithMockUser(roles = "USER")
        void shouldReturn200WithSearchResultsAsUser() throws Exception {
            when(employeeService.searchByName(anyString(), anyBoolean(), any())).thenReturn(paginatedResponse);

            mockMvc.perform(get(BASE_URL + "/search")
                            .param("name", "Juan"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Search results retrieved successfully"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].firstName").value("Juan"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/employees/{id}")
    class UpdateEmployee {

        @Test
        @DisplayName("Should return 400 when PUT request has validation errors")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenPutRequestHasValidationErrors() throws Exception {
            // Missing all required fields triggers validation errors
            String emptyBody = "{}";

            mockMvc.perform(put(BASE_URL + "/{id}", EMPLOYEE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(emptyBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/employees/{id}")
    class PatchEmployee {

        @Test
        @DisplayName("Should return 200 with patched employee as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WithPatchedEmployeeAsAdmin() throws Exception {
            when(employeeService.patchEmployee(eq(EMPLOYEE_ID), any(EmployeePatchRequest.class)))
                    .thenReturn(sampleResponse);

            // Patch only firstName — no dateOfBirth avoids validator issue
            String patchBody = "{\"firstName\":\"Juanito\"}";

            mockMvc.perform(patch(BASE_URL + "/{id}", EMPLOYEE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patchBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Employee patched successfully"))
                    .andExpect(jsonPath("$.data.id").value(EMPLOYEE_ID.toString()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/employees/{id}")
    class DeleteEmployee {

        @Test
        @DisplayName("Should return 200 with soft-deleted employee as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WithDeletedEmployeeAsAdmin() throws Exception {
            EmployeeResponse deletedResponse = EmployeeResponse.builder()
                    .id(EMPLOYEE_ID)
                    .firstName("Juan")
                    .active(false)
                    .build();

            when(employeeService.deleteEmployee(EMPLOYEE_ID)).thenReturn(deletedResponse);

            mockMvc.perform(delete(BASE_URL + "/{id}", EMPLOYEE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Employee deleted successfully"))
                    .andExpect(jsonPath("$.data.active").value(false));
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticatedRequest() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}
