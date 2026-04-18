package com.demo.employees.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DOBValidator} verifying date parsing and minimum age validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DOBValidator Unit Tests")
class DOBValidatorTest {

    private DOBValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @BeforeEach
    void setUp() {
        validator = new DOBValidator();
        ReflectionTestUtils.setField(validator, "minimumAge", 18);
    }

    @Test
    @DisplayName("Should return true for a valid date where employee is at least 18 years old")
    void shouldReturnTrueForValidDateAboveMinimumAge() {
        String validDate = LocalDate.now().minusYears(25).format(FORMAT);

        assertThat(validator.isValid(validDate, context)).isTrue();
    }

    @Test
    @DisplayName("Should return false for a date where employee is under 18 years old")
    void shouldReturnFalseForDateUnderMinimumAge() {
        stubConstraintViolationBuilder();
        String youngDate = LocalDate.now().minusYears(10).format(FORMAT);

        assertThat(validator.isValid(youngDate, context)).isFalse();
    }

    @Test
    @DisplayName("Should return false for a malformed date string")
    void shouldReturnFalseForMalformedDateString() {
        stubConstraintViolationBuilder();

        assertThat(validator.isValid("not-a-date", context)).isFalse();
    }

    @Test
    @DisplayName("Should return true for null input (null handling delegated to @NotBlank)")
    void shouldReturnTrueForNullInput() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    @DisplayName("Should return true when employee is exactly 18 years old today")
    void shouldReturnTrueWhenExactlyMinimumAgeToday() {
        String exactlyEighteen = LocalDate.now().minusYears(18).format(FORMAT);

        assertThat(validator.isValid(exactlyEighteen, context)).isTrue();
    }

    @Test
    @DisplayName("Should return false when employee is one day before turning 18")
    void shouldReturnFalseWhenOneDayBeforeTurningEighteen() {
        stubConstraintViolationBuilder();
        String oneDayShort = LocalDate.now().minusYears(18).plusDays(1).format(FORMAT);

        assertThat(validator.isValid(oneDayShort, context)).isFalse();
    }

    /**
     * Stubs the ConstraintValidatorContext to allow custom message building
     * without throwing NullPointerExceptions.
     */
    private void stubConstraintViolationBuilder() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
    }
}
