package edu.tinkoff.imageeditorapi.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileExtensionValidator.class)
public @interface FileExtensionConstraint {

    String message() default "File extension not allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
