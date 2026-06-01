package br.edu.lms.module.identity.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class UserId {

    String value;

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserId cannot be blank");
        }
        return new UserId(value);
    }
}
