package br.edu.lms.module.identity.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @EqualsAndHashCode.Include
    private final UserId id;

    private final FullName fullName;
    private final Email email;
    private final String passwordHash;
    private UserStatus status;

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public boolean isPendingConfirmation() {
        return UserStatus.PENDING_CONFIRMATION == this.status;
    }
}
