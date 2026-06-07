package br.edu.lms.module.identity.infrastructure.persistence;

import br.edu.lms.module.identity.domain.model.Email;
import br.edu.lms.module.identity.domain.model.User;
import br.edu.lms.module.identity.domain.model.UserId;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final EntityManager em;
    private final UserMapper mapper;

    @Override
    @Transactional
    public User save(User user) {
        var entity = mapper.toEntity(user);
        em.merge(entity);
        return user;
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return em.createQuery(
                        "SELECT u FROM UserJpaEntity u WHERE u.email = :email AND u.deletedAt IS NULL",
                        UserJpaEntity.class)
                .setParameter("email", email.getValue())
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return Optional.ofNullable(em.find(UserJpaEntity.class, id.getValue()))
                .filter(e -> e.getDeletedAt() == null)
                .map(mapper::toDomain);
    }
}
