package com.demo.employees.controller;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import com.demo.employees.dto.request.EmployeePatchRequest;
import com.demo.employees.dto.request.EmployeeRequest;
import com.demo.employees.dto.response.ApiResp;
import com.demo.employees.dto.response.EmployeeResponse;
import com.demo.employees.dto.response.PaginatedResp;
import com.demo.employees.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@Validated
@Tag(name = "Employees", description = "CRUD, search, and pagination operations for employee management")
public class EmployeeController {

    private final Validator validator;
    private final EmployeeService employeeService;

    /**
     * Constructs the controller with the required dependencies.
     *
     * @param employeeService the employee service for business logic delegation
     * @param validator       the JSR-380 validator for manual list-element validation
     */

    public EmployeeController(EmployeeService employeeService, Validator validator) {
        this.employeeService = employeeService;
        this.validator = validator;
    }

    /**
     * Creates one or more employees from a JSON array.
     *
     * @param requests the list of employee creation requests
     * @return a 201 response containing the list of created employees
     */

    @Operation(summary = "Create employees" , description = "Creates one or more employees from a JSON array. Requires ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Employees created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required")
    })
    @PostMapping
    public ResponseEntity<ApiResp<List<EmployeeResponse>>> createEmployees(
            @RequestBody List<EmployeeRequest> requests) {

        Set<ConstraintViolation<EmployeeRequest>> violations = new HashSet<>();

        for (EmployeeRequest request : requests) {
            violations.addAll(validator.validate(request));
        }
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        List<EmployeeResponse> created = employeeService.createEmployees(requests);

        ApiResp<List<EmployeeResponse>> response = ApiResp.success(
                HttpStatus.CREATED.value(),
                "Employees created successfully",
                created
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a paginated list of employees, optionally including soft-deleted records.
     *
     * @param deleted  whether to include soft-deleted employees (default: false)
     * @param pageable pagination and sorting parameters
     * @return a 200 response containing the paginated employee list
     */
    @Operation(summary = "List employees", description = "Returns a paginated list of employees. Use deleted=true to include soft deleted employees.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "false") boolean deleted,
            @PageableDefault(size = 10, page = 0) Pageable pageable){
        PaginatedResp<EmployeeResponse> result = employeeService.getAllEmployees(deleted,pageable);

        ApiResp<PaginatedResp<EmployeeResponse>> response = ApiResp.success(
                HttpStatus.OK.value(),
                "Employees retrieved successfully",
                result
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get employee by ID", description = "Retrieves a single employee by UUID. Use deleted=true to include soft-deleted records.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid UUID format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResp<EmployeeResponse>> getEmployeeById(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean deleted){
        EmployeeResponse result = employeeService.getEmployeeById(id,deleted);

        ApiResp<EmployeeResponse> response = ApiResp.success(
                HttpStatus.OK.value(),
                "Employee retrieved successfully",
                result
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search employees by name", description = "Case-insensitive partial match across all four name fields. Minimum 3 characters required.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Search term too short"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResp<PaginatedResp<EmployeeResponse>>> searchEmployees(
            @RequestParam String name,
            @RequestParam(defaultValue = "false") boolean deleted,
            Pageable pageable){
        PaginatedResp<EmployeeResponse> result = employeeService.searchByName(name,deleted,pageable);

        ApiResp<PaginatedResp<EmployeeResponse>> response = ApiResp.success(
                HttpStatus.OK.value(),
                "Search results retrieved successfully",
                result
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update employee (full replacement)", description = "Replaces all fields of an existing employee. Requires ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResp<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeRequest request
    ){
        EmployeeResponse result = employeeService.updateEmployee(id,request);

        ApiResp<EmployeeResponse> response = ApiResp.success(
                HttpStatus.OK.value(),
                "Employee updated successfully",
                result
        ) ;

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Patch employee (partial update)", description = "Updates only the provided fields, leaving others unchanged. Requires ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee patched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResp<EmployeeResponse>> patchEmployee(
        @PathVariable UUID id,
        @Valid @RequestBody EmployeePatchRequest request
    ){
        EmployeeResponse result = employeeService.patchEmployee(id, request);

        ApiResp<EmployeeResponse> response = ApiResp.success(
            HttpStatus.OK.value(),
                "Employee patched successfully",
                result
        );
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Delete employee (soft delete)", description = "Sets the employee's active status to false. Requires ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResp<EmployeeResponse>> deleteEmployee(@PathVariable UUID id){

        EmployeeResponse result = employeeService.deleteEmployee(id);

        ApiResp<EmployeeResponse> response = ApiResp.success(
                HttpStatus.OK.value(),
                "Employee deleted successfully",
                result
        );

        return ResponseEntity.ok(response);
    }













}
