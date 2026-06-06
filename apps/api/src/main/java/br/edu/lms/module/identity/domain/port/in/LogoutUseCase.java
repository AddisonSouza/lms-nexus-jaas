package br.edu.lms.module.identity.domain.port.in;

public interface LogoutUseCase {
    void execute(String refreshToken);
}
