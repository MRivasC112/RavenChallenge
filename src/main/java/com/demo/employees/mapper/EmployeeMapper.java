package com.demo.employees.mapper;
import com.demo.employees.common.Constants;
import com.demo.employees.dto.request.EmployeePatchRequest;
import com.demo.employees.dto.request.EmployeeRequest;
import com.demo.employees.dto.response.EmployeeResponse;
import com.demo.employees.entity.Employee;
import org.hibernate.query.criteria.internal.predicate.ExistsPredicate;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Mapper(componentModel= "spring")
public abstract class EmployeeMapper {
    protected static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_PATTERN);

    public Employee toEntity(EmployeeRequest request) {
        if (request == null) {
            return null;
        }

        return Employee.builder().firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .fatherName(request.getFatherName())
                .motherName(request.getMotherName())
                .dob(LocalDate.parse(request.getDateOfBirth(), DATE_FORMATTER))
                .gender(request.getGender())
                .position(request.getPosition())
                .build();
    }


    public EmployeeResponse toResponse(Employee entity) {
        if (entity == null) {
            return null;
        }

        return EmployeeResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .middleName(entity.getMiddleName())
                .fatherName(entity.getFatherName())
                .motherName(entity.getMotherName())
                .age(Period.between(entity.getDob(), LocalDate.now()).getYears())
                .dateOfBirth(entity.getDob().format(DATE_FORMATTER))
                .gender(entity.getGender())
                .position(entity.getPosition())
                .registrationDate(entity.getRegistrationDate())
                .active(entity.isActive())
                .build();
    }

    public List<EmployeeResponse> toResponseList(List<Employee> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "registrationDate",ignore = true)
    @Mapping(target = "active",ignore = true)
    @Mapping(target = "dob", expression = "java(java.time.LocalDate.parse(request.getDateOfBirth(), DATE_FORMATTER))")
    public abstract void updateEntityFromRequest(EmployeeRequest request, @MappingTarget Employee entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "dob", expression = "java(request.getDateOfBirth() != null ? java.time.LocalDate.parse(request.getDateOfBirth(), DATE_FORMATTER) : entity.getDob())")
    public abstract void patchEntityFromRequest(EmployeePatchRequest request, @MappingTarget Employee entity);

}
