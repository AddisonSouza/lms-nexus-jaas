package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.domain.exception.EmailAlreadyConfirmedException;
import br.edu.lms.module.identity.domain.exception.InvalidConfirmationTokenException;
import br.edu.lms.module.identity.domain.model.UserId;
import br.edu.lms.module.identity.domain.port.in.ConfirmEmailUseCase;
import br.edu.lms.module.identity.domain.port.out.EmailConfirmationTokenRepository;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ConfirmEmailService implements ConfirmEmailUseCase {

    private final EmailConfirmationTokenRepository confirmationTokenRepository;
    private final UserRepository userRepository;

    @Override
    public void execute(String token) {
        var userId = confirmationTokenRepository.findUserId(token)
                .orElseThrow(InvalidConfirmationTokenException::new);

        var user = userRepository.findById(UserId.of(userId))
                .orElseThrow(InvalidConfirmationTokenException::new);

        if (!user.isPendingConfirmation()) {
            throw new EmailAlreadyConfirmedException();
        }

        user.activate();
        userRepository.save(user);
        // O token não é invalidado aqui: a chave `ect:{token}` expira sozinha pelo
        // TTL de 24h. Apagá-la fazia o segundo clique no link cair em
        // `InvalidConfirmationTokenException` ("Link inválido ou expirado") em vez
        // do 409 idempotente, alarmando quem só clicou duas vezes. O replay é
        // inócuo: a única ação que o token habilita é `activate()`, já guardada
        // pelo status logo acima.

        log.info("Email confirmed for user: {}", userId);
    }
}
