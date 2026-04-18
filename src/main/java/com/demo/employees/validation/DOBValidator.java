package com.demo.employees.validation;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DOBValidator implements ConstraintValidator<ValidDOB, String> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    @Value("${employee.minimum-age:18}")
    private int minimumAge;

    @Override
    public void initialize(ValidDOB constraintAnnotation) {
        // minimumAge is injected from application.yml via @Value
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(value, FORMATTER);
        } catch (DateTimeParseException e) {
            buildCustomMessage(context, "Date of birth must be in dd-MM-yyyy format");
            return false;
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < minimumAge) {
            buildCustomMessage(context,
                    String.format("Employee must be at least %d years old", minimumAge));
            return false;
        }

        return true;
    }

    private void buildCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
