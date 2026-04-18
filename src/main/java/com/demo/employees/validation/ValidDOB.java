package com.demo.employees.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DOBValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDOB {

    /**
     * Default validation message. The {@code {minAge}} placeholder is resolved
     * by the validator at runtime from the configured minimum age.
     *
     * @return the error message template
     */
    String message() default "Employee must be at least {minAge} years old";


    /**
     * Validation groups this constraint belongs to.
     *
     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload for clients to assign custom metadata to a constraint.
     *
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Minimum age required for the employee. Defaults to 18 but is overridden
     * by the {@code employee.minimum-age} property in the validator.
     *
     * @return the minimum age
     */
    int minAge() default 18;
}
