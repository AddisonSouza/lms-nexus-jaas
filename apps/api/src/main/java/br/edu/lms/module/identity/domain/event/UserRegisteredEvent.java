package br.edu.lms.module.identity.domain.event;

import br.edu.lms.module.identity.domain.model.Email;
import br.edu.lms.module.identity.domain.model.UserId;

public record UserRegisteredEvent(UserId userId, Email email) {
}
