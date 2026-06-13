package br.edu.lms.module.classroom.domain.model;

import lombok.Value;

import java.security.SecureRandom;

@Value
public class InviteCode {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    String value;

    public static InviteCode generate() {
        var sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return new InviteCode(sb.toString());
    }

    public static InviteCode of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("InviteCode cannot be blank");
        }
        return new InviteCode(value);
    }
}
