package br.edu.lms.module.communication.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class AnnouncementId {

    String value;

    public static AnnouncementId generate() {
        return new AnnouncementId(UUID.randomUUID().toString());
    }

    public static AnnouncementId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AnnouncementId cannot be blank");
        }
        return new AnnouncementId(value);
    }
}
