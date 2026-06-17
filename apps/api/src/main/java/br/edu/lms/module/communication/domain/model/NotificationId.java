package br.edu.lms.module.communication.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class NotificationId {

    String value;

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID().toString());
    }

    public static NotificationId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("NotificationId cannot be blank");
        }
        return new NotificationId(value);
    }
}
