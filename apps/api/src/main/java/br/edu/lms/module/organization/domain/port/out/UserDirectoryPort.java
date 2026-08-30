package br.edu.lms.module.organization.domain.port.out;

import br.edu.lms.module.organization.domain.model.UserProfile;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Read-only lookup of user data, owned by the identity module.
 * Lets the organization module identify users without depending on identity internals.
 */
public interface UserDirectoryPort {

    /**
     * @return the user's email, or empty when the id is unknown.
     */
    Optional<String> findEmailById(String userId);

    /**
     * @return a map of userId -> profile for the given ids (missing ids are absent).
     */
    Map<String, UserProfile> findProfilesByIds(Collection<String> userIds);
}
