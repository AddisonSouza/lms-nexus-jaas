package br.edu.lms.module.identity.domain.model;

import br.edu.lms.shared.domain.DomainException;
import lombok.Value;

@Value
public class Email {

    String value;

    public Email(String value) {
        if (value == null || !value.contains("@") || value.isBlank()) {
            throw new DomainException("E-mail inválido: " + value);
        }
        this.value = value.toLowerCase().trim();
    }
}
