package br.edu.lms.module.curriculum.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class TopicId {
    String value;

    public static TopicId generate() {
        return new TopicId(UUID.randomUUID().toString());
    }

    public static TopicId of(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("TopicId cannot be blank");
        return new TopicId(value);
    }
}
