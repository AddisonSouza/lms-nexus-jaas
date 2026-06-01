package br.edu.lms.module.identity.domain.port.out;

import br.edu.lms.module.identity.domain.model.Email;
import br.edu.lms.module.identity.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(Email email);
}
