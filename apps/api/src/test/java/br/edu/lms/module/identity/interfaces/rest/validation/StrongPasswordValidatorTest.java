package br.edu.lms.module.identity.interfaces.rest.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    record Form(@StrongPassword String password) {}

    static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
    }

    private List<String> messagesFor(String password) {
        return validator.validate(new Form(password)).stream()
                .map(ConstraintViolation::getMessage)
                .toList();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Senha@12345", "Aa1!aaaa", "Ab3 cdefg", "Çaa1Aaaa"})
    void acceptsPasswordMeetingAllCriteria(String password) {
        assertThat(messagesFor(password)).isEmpty();
    }

    @Test
    void acceptsNullSoNotBlankOwnsTheRequiredCheck() {
        assertThat(messagesFor(null)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Aa1!aaa"})
    void rejectsPasswordShorterThanMinimum(String password) {
        assertThat(messagesFor(password)).containsExactly("Senha deve ter no mínimo 8 caracteres");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "aaaa1!aa | A senha precisa de: uma maiúscula",
            "AAAA1!AA | A senha precisa de: uma minúscula",
            "Aaaaa!aa | A senha precisa de: um número",
            "Aaaaa1aa | A senha precisa de: um símbolo",
            "newpassword123 | A senha precisa de: uma maiúscula, um símbolo",
            "aaaaaaaa | A senha precisa de: uma maiúscula, um número, um símbolo"
    })
    void listsOnlyTheMissingCriteria(String password, String expected) {
        assertThat(messagesFor(password)).containsExactly(expected);
    }
}
