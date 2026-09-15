package br.edu.lms.module.identity.interfaces.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Senha segura: mínimo de 8 caracteres com maiúscula, minúscula, número e símbolo.
 * Espelha o {@code passwordSchema} do front. Valor nulo é aceito — use {@code @NotBlank}.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Retention(RUNTIME)
public @interface StrongPassword {

    String message() default "Senha fraca";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
