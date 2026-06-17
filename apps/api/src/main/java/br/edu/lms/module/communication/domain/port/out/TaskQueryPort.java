package br.edu.lms.module.communication.domain.port.out;

import java.util.Optional;

public interface TaskQueryPort {
    Optional<String> findSubjectIdByTask(String taskId);
}
