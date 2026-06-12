package br.edu.lms.module.identity.domain.port.in;

public interface ConfirmEmailUseCase {
    void execute(String token);
}
