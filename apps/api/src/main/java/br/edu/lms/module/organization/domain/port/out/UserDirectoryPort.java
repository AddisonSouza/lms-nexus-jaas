package br.edu.lms.module.organization.domain.port.out;

import java.util.Optional;

/**
 * Read-only lookup of user emails, owned by the identity module.
 * Lets the organization module check who is accepting an invitation without
 * depending on identity internals.
 */
public interface UserDirectoryPort {

    /**
     * @return the user's email, or empty when the id is unknown.
     */
    Optional<String> findEmailById(String userId);
}
