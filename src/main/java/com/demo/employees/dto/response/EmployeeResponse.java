package com.demo.employees.dto.response;


import com.demo.employees.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private String firstName;
    private String middleName;
    private String fatherName;
    private String motherName;
    private int age;
    private String dateOfBirth;
    private Gender gender;
    private String position;
    private LocalDateTime registrationDate;
    private boolean active;
}
