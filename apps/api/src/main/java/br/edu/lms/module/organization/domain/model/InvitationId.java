package br.edu.lms.module.organization.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class InvitationId {

    String value;

    public static InvitationId generate() {
        return new InvitationId(UUID.randomUUID().toString());
    }

    public static InvitationId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("InvitationId cannot be blank");
        }
        return new InvitationId(value);
    }
}
