package com.demo.employees.dto.request;


import com.demo.employees.common.Constants;
import com.demo.employees.enums.Gender;
import com.demo.employees.validation.ValidDOB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(max = Constants.NAME_MAX_LENGTH, message = "First name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "First name contains invalid characters")
    private String firstName;


    @Size(max = Constants.NAME_MAX_LENGTH, message = "Middle name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "Middle name contains invalid characters")
    private String middleName;

    @NotBlank(message = "Paternal last name is required")
    @Size(max = Constants.NAME_MAX_LENGTH, message = "Paternal last name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "Paternal last contains invalid characters")
    private String fatherName;

    @NotBlank(message = "Maternal last name is required")
    @Size(max = Constants.NAME_MAX_LENGTH, message = "Maternal last name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "Maternal last contains invalid characters")
    private String motherName;

    @NotBlank(message = "Date of birth is required")
    @ValidDOB
    private String dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Position is required")
    private String position;

}
