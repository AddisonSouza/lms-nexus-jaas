package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.EvaluateSubmissionCommand;
import br.edu.lms.module.assessment.application.dto.SubmissionResponse;
import br.edu.lms.module.assessment.domain.event.SubmissionEvaluatedEvent;
import br.edu.lms.module.assessment.domain.exception.GradeExceedsMaxScoreException;
import br.edu.lms.module.assessment.domain.exception.GradeNotAllowedException;
import br.edu.lms.module.assessment.domain.exception.SubmissionNotFoundException;
import br.edu.lms.module.assessment.domain.exception.TaskNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.SubmissionId;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.port.in.EvaluateSubmissionUseCase;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class EvaluateSubmissionService implements EvaluateSubmissionUseCase {

    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final Event<SubmissionEvaluatedEvent> evaluatedEvent;

    @Override
    public SubmissionResponse execute(EvaluateSubmissionCommand command) {
        var submission = submissionRepository.findById(SubmissionId.of(command.getSubmissionId()))
                .orElseThrow(() -> new SubmissionNotFoundException(command.getSubmissionId()));

        var task = taskRepository.findByIdAndOrganization(TaskId.of(submission.getTaskId()), command.getOrganizationId())
                .orElseThrow(() -> new TaskNotFoundException(submission.getTaskId()));

        if (!task.getCreatedBy().equals(command.getProfessorId())) {
            throw new UnauthorizedTaskOperationException(command.getProfessorId(), submission.getTaskId());
        }

        if (command.getGrade() != null) {
            if (task.getMaxScore() == null) {
                throw new GradeNotAllowedException(submission.getTaskId());
            }
            if (command.getGrade().compareTo(task.getMaxScore()) > 0) {
                throw new GradeExceedsMaxScoreException(command.getGrade(), task.getMaxScore());
            }
        }

        var evaluated = submission.evaluate(command.getGrade(), command.getFeedback());

        var saved = submissionRepository.save(evaluated);
        evaluatedEvent.fire(new SubmissionEvaluatedEvent(
                saved.getId().getValue(), saved.getTaskId(), saved.getStudentId(), saved.getOrganizationId()));
        log.info("Submission evaluated: submission={} task={} professor={}",
                saved.getId().getValue(), saved.getTaskId(), command.getProfessorId());

        return SubmitTaskService.toResponse(saved);
    }
}
