package br.edu.lms.module.identity.domain.port.out;

import br.edu.lms.module.identity.domain.model.Email;

public interface EmailPort {
    void sendConfirmationEmail(Email to, String token);
    void sendPasswordResetEmail(Email to, String token);
}
