package br.edu.lms.module.curriculum.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class SubjectContentId {
    String value;

    public static SubjectContentId generate() {
        return new SubjectContentId(UUID.randomUUID().toString());
    }

    public static SubjectContentId of(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("SubjectContentId cannot be blank");
        return new SubjectContentId(value);
    }
}
