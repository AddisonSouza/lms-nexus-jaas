package br.edu.lms.module.curriculum.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class SubjectId {

    String value;

    public static SubjectId generate() {
        return new SubjectId(UUID.randomUUID().toString());
    }

    public static SubjectId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SubjectId cannot be blank");
        }
        return new SubjectId(value);
    }
}
