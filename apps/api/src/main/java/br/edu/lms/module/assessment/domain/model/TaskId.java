package br.edu.lms.module.assessment.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class TaskId {

    String value;

    public static TaskId generate() {
        return new TaskId(UUID.randomUUID().toString());
    }

    public static TaskId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TaskId cannot be blank");
        }
        return new TaskId(value);
    }
}
