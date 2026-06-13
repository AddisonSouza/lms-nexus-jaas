package br.edu.lms.module.classroom.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class ClassroomId {

    String value;

    public static ClassroomId generate() {
        return new ClassroomId(UUID.randomUUID().toString());
    }

    public static ClassroomId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ClassroomId cannot be blank");
        }
        return new ClassroomId(value);
    }
}
