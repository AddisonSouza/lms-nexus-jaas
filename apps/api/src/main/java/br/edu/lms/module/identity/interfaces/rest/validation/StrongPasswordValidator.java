package br.edu.lms.module.identity.interfaces.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    static final int MIN_LENGTH = 8;

    private record Criterion(String label, Predicate<String> isMet) {
        static Criterion matching(String label, String regex) {
            return new Criterion(label, Pattern.compile(regex).asPredicate());
        }
    }

    private static final List<Criterion> CRITERIA = List.of(
            Criterion.matching("uma maiúscula", "[A-Z]"),
            Criterion.matching("uma minúscula", "[a-z]"),
            Criterion.matching("um número", "\\d"),
            Criterion.matching("um símbolo", "[^A-Za-z0-9]")
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String message = violationMessage(value);
        if (message == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }

    private static String violationMessage(String value) {
        if (value.length() < MIN_LENGTH) {
            return "Senha deve ter no mínimo " + MIN_LENGTH + " caracteres";
        }

        String missing = CRITERIA.stream()
                .filter(criterion -> !criterion.isMet().test(value))
                .map(Criterion::label)
                .collect(Collectors.joining(", "));
        return missing.isEmpty() ? null : "A senha precisa de: " + missing;
    }
}
