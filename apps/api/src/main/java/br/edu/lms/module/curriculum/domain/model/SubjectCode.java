package br.edu.lms.module.curriculum.domain.model;

import lombok.Value;

@Value
public class SubjectCode {

    String value;

    public SubjectCode(String value) {
        if (value != null && value.length() > 20) {
            throw new IllegalArgumentException("SubjectCode must be at most 20 characters");
        }
        this.value = value;
    }

    public static SubjectCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new SubjectCode(value.trim().toUpperCase());
    }
}
