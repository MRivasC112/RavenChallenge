package com.demo.employees.service;

import com.demo.employees.dto.request.EmployeePatchRequest;
import com.demo.employees.dto.request.EmployeeRequest;
import com.demo.employees.dto.response.EmployeeResponse;
import com.demo.employees.dto.response.PaginatedResp;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    /**
     * Creates one or more employees from the given list of requests.
     *
     * @param requests the list of employee creation requests
     * @return a list of created employee responses with generated IDs and registration timestamps
     */
    List<EmployeeResponse> createEmployees(List<EmployeeRequest> requests);

    PaginatedResp<EmployeeResponse> getAllEmployees(boolean deleted, Pageable pageable);

    EmployeeResponse getEmployeeById(UUID id, boolean includeDeleted);

    PaginatedResp<EmployeeResponse> searchByName(String name, boolean includeDeleted, Pageable pageable);

    EmployeeResponse updateEmployee(UUID id, EmployeeRequest request);

    EmployeeResponse patchEmployee(UUID id, EmployeePatchRequest request);

    EmployeeResponse deleteEmployee(UUID id);
}
