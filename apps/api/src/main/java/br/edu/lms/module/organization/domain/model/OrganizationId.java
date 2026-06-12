package br.edu.lms.module.organization.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class OrganizationId {

    String value;

    public static OrganizationId generate() {
        return new OrganizationId(UUID.randomUUID().toString());
    }

    public static OrganizationId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrganizationId cannot be blank");
        }
        return new OrganizationId(value);
    }
}
