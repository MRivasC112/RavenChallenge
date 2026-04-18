package com.demo.employees.entity;

import com.demo.employees.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue
    @Type(type="uuid-char")
    @Column(name = "id",updatable = false,nullable = false, columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "first_name",nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "father_name", nullable = false, length = 50)
    private String fatherName;

    @Column(name = "mother_name", nullable = false, length = 50)
    private String motherName;

    @Column(name = "DOB",  nullable = false, length = 50)
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(name="gender", nullable = false, length = 20)
    private Gender gender;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "registration_date", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column(name = "active",nullable = false)
    private boolean active;
}
