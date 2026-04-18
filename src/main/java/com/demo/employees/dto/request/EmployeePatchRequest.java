package com.demo.employees.dto.request;

import com.demo.employees.common.Constants;
import com.demo.employees.enums.Gender;
import com.demo.employees.validation.ValidDOB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePatchRequest {
    @Size(max = Constants.NAME_MAX_LENGTH, message = "First name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "First name contains invalid characters")
    private String firstName;

    @Size(max = Constants.NAME_MAX_LENGTH, message = "Middle name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "Middle name contains invalid characters")
    private String middleName;

    @Size(max = Constants.NAME_MAX_LENGTH, message = "Paternal last name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "Paternal last name contains invalid characters")
    private String paternalLastName;

    @Size(max = Constants.NAME_MAX_LENGTH, message = "Maternal last name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "Maternal last name contains invalid characters")
    private String maternalLastName;

    @ValidDOB
    private String dateOfBirth;

    private Gender gender;

    private String position;
}
