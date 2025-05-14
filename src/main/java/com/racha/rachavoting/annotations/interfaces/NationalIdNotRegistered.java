package com.racha.rachavoting.annotations.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.racha.rachavoting.annotations.NationalIdValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = NationalIdValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NationalIdNotRegistered {
    String message() default "ID not valid or already registered";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}