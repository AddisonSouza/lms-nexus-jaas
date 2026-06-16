package br.edu.lms.module.assessment.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class SubmissionId {

    String value;

    public static SubmissionId generate() {
        return new SubmissionId(UUID.randomUUID().toString());
    }

    public static SubmissionId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SubmissionId cannot be blank");
        }
        return new SubmissionId(value);
    }
}
