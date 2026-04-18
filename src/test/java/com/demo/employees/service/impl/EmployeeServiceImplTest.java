package com.demo.employees.service.impl;

import com.demo.employees.dto.request.EmployeePatchRequest;
import com.demo.employees.dto.request.EmployeeRequest;
import com.demo.employees.dto.response.EmployeeResponse;
import com.demo.employees.dto.response.PaginatedResp;
import com.demo.employees.entity.Employee;
import com.demo.employees.enums.Gender;
import com.demo.employees.exception.EmployeeNotFoundException;
import com.demo.employees.exception.SearchTermTooShortException;
import com.demo.employees.mapper.EmployeeMapper;
import com.demo.employees.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EmployeeServiceImpl} verifying business logic in isolation
 * with mocked repository and mapper dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeServiceImpl Unit Tests")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Captor
    private ArgumentCaptor<List<Employee>> employeeListCaptor;

    private EmployeeServiceImpl employeeService;

    private static final int SEARCH_MIN_LENGTH = 3;
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();

    private Employee sampleEmployee;
    private EmployeeResponse sampleResponse;
    private EmployeeRequest sampleRequest;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, employeeMapper, SEARCH_MIN_LENGTH);

        sampleEmployee = Employee.builder()
                .id(EMPLOYEE_ID)
                .firstName("Juan")
                .middleName("Carlos")
                .fatherName("Garcia")
                .motherName("Lopez")
                .dob(LocalDate.of(1990, 5, 15))
                .gender(Gender.MALE)
                .position("Developer")
                .registrationDate(LocalDateTime.now())
                .active(true)
                .build();

        sampleResponse = EmployeeResponse.builder()
                .id(EMPLOYEE_ID)
                .firstName("Juan")
                .middleName("Carlos")
                .fatherName("Garcia")
                .motherName("Lopez")
                .age(34)
                .dateOfBirth("15-05-1990")
                .gender(Gender.MALE)
                .position("Developer")
                .registrationDate(sampleEmployee.getRegistrationDate())
                .active(true)
                .build();

        sampleRequest = EmployeeRequest.builder()
                .firstName("Juan")
                .middleName("Carlos")
                .fatherName("Garcia")
                .motherName("Lopez")
                .dateOfBirth("15-05-1990")
                .gender(Gender.MALE)
                .position("Developer")
                .build();
    }

    @Nested
    @DisplayName("createEmployees")
    class CreateEmployees {

        @Test
        @DisplayName("Should create employees with registration date and active status set to true")
        void shouldCreateEmployeesWithRegistrationDateAndActiveTrue() {
            Employee mappedEntity = Employee.builder()
                    .firstName("Juan")
                    .fatherName("Garcia")
                    .motherName("Lopez")
                    .dob(LocalDate.of(1990, 5, 15))
                    .gender(Gender.MALE)
                    .position("Developer")
                    .build();

            when(employeeMapper.toEntity(any(EmployeeRequest.class))).thenReturn(mappedEntity);
            when(employeeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(employeeMapper.toResponseList(anyList())).thenReturn(List.of(sampleResponse));

            List<EmployeeResponse> result = employeeService.createEmployees(List.of(sampleRequest));

            verify(employeeRepository).saveAll(employeeListCaptor.capture());
            List<Employee> savedEmployees = employeeListCaptor.getValue();

            assertThat(savedEmployees).hasSize(1);
            assertThat(savedEmployees.get(0).isActive()).isTrue();
            assertThat(savedEmployees.get(0).getRegistrationDate()).isNotNull();
            verify(employeeMapper).toEntity(sampleRequest);
            verify(employeeMapper).toResponseList(anyList());
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAllEmployees")
    class GetAllEmployees {

        private final Pageable pageable = PageRequest.of(0, 20);

        @Test
        @DisplayName("Should call findByActiveTrue when includeDeleted is false")
        void shouldCallFindByActiveTrueWhenIncludeDeletedFalse() {
            Page<Employee> page = new PageImpl<>(List.of(sampleEmployee), pageable, 1);
            when(employeeRepository.findByActiveTrue(pageable)).thenReturn(page);
            when(employeeMapper.toResponse(any(Employee.class))).thenReturn(sampleResponse);

            PaginatedResp<EmployeeResponse> result = employeeService.getAllEmployees(false, pageable);

            verify(employeeRepository).findByActiveTrue(pageable);
            verify(employeeRepository, never()).findAll(pageable);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should call findAll when includeDeleted is true")
        void shouldCallFindAllWhenIncludeDeletedTrue() {
            Page<Employee> page = new PageImpl<>(List.of(sampleEmployee), pageable, 1);
            when(employeeRepository.findAll(pageable)).thenReturn(page);
            when(employeeMapper.toResponse(any(Employee.class))).thenReturn(sampleResponse);

            PaginatedResp<EmployeeResponse> result = employeeService.getAllEmployees(true, pageable);

            verify(employeeRepository).findAll(pageable);
            verify(employeeRepository, never()).findByActiveTrue(pageable);
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeById {

        @Test
        @DisplayName("Should return employee when found by ID and active")
        void shouldReturnEmployeeWhenFoundByIdAndActive() {
            when(employeeRepository.findByIdAndActiveTrue(EMPLOYEE_ID)).thenReturn(Optional.of(sampleEmployee));
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.getEmployeeById(EMPLOYEE_ID, false);

            verify(employeeRepository).findByIdAndActiveTrue(EMPLOYEE_ID);
            assertThat(result).isEqualTo(sampleResponse);
        }

        @Test
        @DisplayName("Should throw EmployeeNotFoundException when employee not found")
        void shouldThrowNotFoundWhenEmployeeDoesNotExist() {
            when(employeeRepository.findByIdAndActiveTrue(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(EMPLOYEE_ID, false))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessageContaining(EMPLOYEE_ID.toString());
        }

        @Test
        @DisplayName("Should call findById when includeDeleted is true")
        void shouldCallFindByIdWhenIncludeDeletedTrue() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(sampleEmployee));
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.getEmployeeById(EMPLOYEE_ID, true);

            verify(employeeRepository).findById(EMPLOYEE_ID);
            verify(employeeRepository, never()).findByIdAndActiveTrue(EMPLOYEE_ID);
            assertThat(result).isEqualTo(sampleResponse);
        }
    }

    @Nested
    @DisplayName("searchByName")
    class SearchByName {

        private final Pageable pageable = PageRequest.of(0, 20);

        @Test
        @DisplayName("Should search active employees when search term is valid")
        void shouldSearchActiveEmployeesWhenSearchTermValid() {
            Page<Employee> page = new PageImpl<>(List.of(sampleEmployee), pageable, 1);
            when(employeeRepository.searchByNameActive("Juan", pageable)).thenReturn(page);
            when(employeeMapper.toResponse(any(Employee.class))).thenReturn(sampleResponse);

            PaginatedResp<EmployeeResponse> result = employeeService.searchByName("Juan", false, pageable);

            verify(employeeRepository).searchByNameActive("Juan", pageable);
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw SearchTermTooShortException when search term is too short")
        void shouldThrowSearchTermTooShortWhenTermTooShort() {
            assertThatThrownBy(() -> employeeService.searchByName("ab", false, pageable))
                    .isInstanceOf(SearchTermTooShortException.class)
                    .hasMessageContaining(String.valueOf(SEARCH_MIN_LENGTH));
        }

        @Test
        @DisplayName("Should call searchByNameAll when includeDeleted is true")
        void shouldCallSearchByNameAllWhenIncludeDeletedTrue() {
            Page<Employee> page = new PageImpl<>(List.of(sampleEmployee), pageable, 1);
            when(employeeRepository.searchByNameAll("Juan", pageable)).thenReturn(page);
            when(employeeMapper.toResponse(any(Employee.class))).thenReturn(sampleResponse);

            PaginatedResp<EmployeeResponse> result = employeeService.searchByName("Juan", true, pageable);

            verify(employeeRepository).searchByNameAll("Juan", pageable);
            verify(employeeRepository, never()).searchByNameActive("Juan", pageable);
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployee {

        @Test
        @DisplayName("Should update employee when found and return updated response")
        void shouldUpdateEmployeeWhenFoundAndReturnUpdatedResponse() {
            Employee savedEmployee = Employee.builder()
                    .id(EMPLOYEE_ID)
                    .firstName("Updated")
                    .fatherName("Garcia")
                    .motherName("Lopez")
                    .dob(LocalDate.of(1990, 5, 15))
                    .gender(Gender.MALE)
                    .position("Senior Developer")
                    .registrationDate(sampleEmployee.getRegistrationDate())
                    .active(true)
                    .build();

            when(employeeRepository.findByIdAndActiveTrue(EMPLOYEE_ID)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);
            when(employeeMapper.toResponse(savedEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.updateEmployee(EMPLOYEE_ID, sampleRequest);

            verify(employeeMapper).updateEntityFromRequest(sampleRequest, sampleEmployee);
            verify(employeeRepository).save(sampleEmployee);
            assertThat(result).isEqualTo(sampleResponse);
        }

        @Test
        @DisplayName("Should throw EmployeeNotFoundException when employee not found for update")
        void shouldThrowNotFoundWhenEmployeeNotFoundForUpdate() {
            when(employeeRepository.findByIdAndActiveTrue(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateEmployee(EMPLOYEE_ID, sampleRequest))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessageContaining(EMPLOYEE_ID.toString());

            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("patchEmployee")
    class PatchEmployee {

        @Test
        @DisplayName("Should patch employee when found and return updated response")
        void shouldPatchEmployeeWhenFoundAndReturnUpdatedResponse() {
            EmployeePatchRequest patchRequest = EmployeePatchRequest.builder()
                    .firstName("Juanito")
                    .build();

            when(employeeRepository.findByIdAndActiveTrue(EMPLOYEE_ID)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);
            when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

            EmployeeResponse result = employeeService.patchEmployee(EMPLOYEE_ID, patchRequest);

            verify(employeeMapper).patchEntityFromRequest(patchRequest, sampleEmployee);
            verify(employeeRepository).save(sampleEmployee);
            assertThat(result).isEqualTo(sampleResponse);
        }

        @Test
        @DisplayName("Should throw EmployeeNotFoundException when employee not found for patch")
        void shouldThrowNotFoundWhenEmployeeNotFoundForPatch() {
            EmployeePatchRequest patchRequest = EmployeePatchRequest.builder()
                    .firstName("Juanito")
                    .build();

            when(employeeRepository.findByIdAndActiveTrue(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.patchEmployee(EMPLOYEE_ID, patchRequest))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessageContaining(EMPLOYEE_ID.toString());

            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployee {

        @Test
        @DisplayName("Should soft-delete employee by setting active to false")
        void shouldSoftDeleteEmployeeBySettingActiveToFalse() {
            Employee deletedEmployee = Employee.builder()
                    .id(EMPLOYEE_ID)
                    .firstName("Juan")
                    .fatherName("Garcia")
                    .motherName("Lopez")
                    .dob(LocalDate.of(1990, 5, 15))
                    .gender(Gender.MALE)
                    .position("Developer")
                    .registrationDate(sampleEmployee.getRegistrationDate())
                    .active(false)
                    .build();

            EmployeeResponse deletedResponse = EmployeeResponse.builder()
                    .id(EMPLOYEE_ID)
                    .active(false)
                    .build();

            when(employeeRepository.findByIdAndActiveTrue(EMPLOYEE_ID)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(deletedEmployee);
            when(employeeMapper.toResponse(deletedEmployee)).thenReturn(deletedResponse);

            EmployeeResponse result = employeeService.deleteEmployee(EMPLOYEE_ID);

            assertThat(sampleEmployee.isActive()).isFalse();
            verify(employeeRepository).save(sampleEmployee);
            assertThat(result.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw EmployeeNotFoundException when employee not found for delete")
        void shouldThrowNotFoundWhenEmployeeNotFoundForDelete() {
            when(employeeRepository.findByIdAndActiveTrue(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessageContaining(EMPLOYEE_ID.toString());

            verify(employeeRepository, never()).save(any());
        }
    }
}
