package com.cts.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = ValidDateValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidDate {

    String message() default "Invalid date format. Expected dd-MM-yyyy";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}