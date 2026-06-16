package br.edu.lms.module.assessment.domain.port.out;

import br.edu.lms.module.assessment.domain.model.SubmissionId;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;

import java.util.Optional;

public interface SubmissionRepository {
    TaskSubmission save(TaskSubmission submission);
    Optional<TaskSubmission> findById(SubmissionId id);
    Optional<TaskSubmission> findByTaskAndStudent(String taskId, String studentId);
}
