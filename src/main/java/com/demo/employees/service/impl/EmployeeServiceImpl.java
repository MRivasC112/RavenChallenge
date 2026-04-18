package com.demo.employees.service.impl;

import com.demo.employees.exception.EmployeeNotFoundException;
import com.demo.employees.exception.SearchTermTooShortException;
import com.demo.employees.dto.request.EmployeePatchRequest;
import com.demo.employees.dto.request.EmployeeRequest;
import com.demo.employees.dto.response.EmployeeResponse;
import com.demo.employees.dto.response.PaginatedResp;
import com.demo.employees.entity.Employee;
import com.demo.employees.mapper.EmployeeMapper;
import com.demo.employees.repository.EmployeeRepository;
import com.demo.employees.service.EmployeeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    public final EmployeeRepository employeeRepository;
    public final EmployeeMapper employeeMapper;
    public final int searchMinLength;

    /**
     * Constructs the service with required dependencies.
     *
     * @param employeeRepository the employee JPA repository
     * @param employeeMapper     the MapStruct mapper for entity/DTO conversion
     * @param searchMinLength    the minimum search term length from configuration
     */
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               EmployeeMapper employeeMapper,
                               @Value("${employee.search.min-length}") int searchMinLength) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.searchMinLength = searchMinLength;
    }

    @Override
    public List<EmployeeResponse> createEmployees(List<EmployeeRequest> requests){
        List<Employee> employees = requests.stream()
                .map(request -> {
                    Employee entity = employeeMapper.toEntity(request);
                    entity.setRegistrationDate(LocalDateTime.now());
                    entity.setActive(true);
                    return entity;
                })
                .collect(Collectors.toList());
        List<Employee> saved = employeeRepository.saveAll(employees);
        return employeeMapper.toResponseList(saved);
    }

    @Override
    public PaginatedResp<EmployeeResponse> getAllEmployees(boolean includeDeleted, Pageable pageable) {
        Page<Employee> page = includeDeleted
                ? employeeRepository.findAll(pageable)
                : employeeRepository.findByActiveTrue(pageable);
        return buildPaginatedResponse(page);
    }


    @Override
    public EmployeeResponse getEmployeeById(UUID id, boolean includeDeleted) {

        Employee employee = includeDeleted
                ? employeeRepository.findById(id)
                .orElseThrow(()-> new EmployeeNotFoundException(id))
                : employeeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(()-> new EmployeeNotFoundException(id));

        return employeeMapper.toResponse(employee);
    }

    @Override
    public PaginatedResp<EmployeeResponse> searchByName(String name, boolean includeDeleted, Pageable pageable) {
        if(name == null || name.trim().length()< searchMinLength){
            throw new SearchTermTooShortException(searchMinLength);
        }
        Page<Employee> page = includeDeleted
                ?employeeRepository.searchByNameAll(name, pageable)
                : employeeRepository.searchByNameActive(name,pageable);
        return buildPaginatedResponse(page);
    }





    @Override
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request){
        Employee employee = employeeRepository.findByIdAndActiveTrue(id).
                orElseThrow(()-> new EmployeeNotFoundException(id));
        employeeMapper.updateEntityFromRequest(request, employee);
        Employee saved = employeeRepository.save(employee);

        return employeeMapper.toResponse(saved);
    }

    @Override
    public EmployeeResponse patchEmployee(UUID id, EmployeePatchRequest request) {
        Employee employee = employeeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        employeeMapper.patchEntityFromRequest(request, employee);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    @Override
    public EmployeeResponse deleteEmployee(UUID id) {
        Employee employee = employeeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        employee.setActive(false);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    private PaginatedResp<EmployeeResponse> buildPaginatedResponse(Page<Employee> page) {
        List<EmployeeResponse> content = page.getContent().stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());

        return PaginatedResp.<EmployeeResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }


}
