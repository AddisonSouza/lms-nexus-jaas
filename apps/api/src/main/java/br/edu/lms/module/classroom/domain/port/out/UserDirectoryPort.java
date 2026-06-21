package br.edu.lms.module.classroom.domain.port.out;

import java.util.Collection;
import java.util.Map;

/**
 * Read-only lookup of user display names, owned by the identity module.
 * Lets the classroom module show member names without depending on identity internals.
 */
public interface UserDirectoryPort {

    /**
     * @return a map of userId -> full name for the given ids (missing ids are absent).
     */
    Map<String, String> findNamesByIds(Collection<String> userIds);
}
