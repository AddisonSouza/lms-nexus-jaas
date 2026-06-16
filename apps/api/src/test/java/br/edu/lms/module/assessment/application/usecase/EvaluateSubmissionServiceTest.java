package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.EvaluateSubmissionCommand;
import br.edu.lms.module.assessment.domain.event.SubmissionEvaluatedEvent;
import br.edu.lms.module.assessment.domain.exception.GradeExceedsMaxScoreException;
import br.edu.lms.module.assessment.domain.exception.GradeNotAllowedException;
import br.edu.lms.module.assessment.domain.exception.SubmissionAlreadyEvaluatedException;
import br.edu.lms.module.assessment.domain.exception.SubmissionNotFoundException;
import br.edu.lms.module.assessment.domain.exception.TaskNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.*;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateSubmissionServiceTest {

    @Mock SubmissionRepository submissionRepository;
    @Mock TaskRepository taskRepository;
    @Mock Event<SubmissionEvaluatedEvent> evaluatedEvent;
    @InjectMocks EvaluateSubmissionService service;

    private static final String ORG_ID = "org-1";
    private static final String PROF_ID = "prof-1";
    private static final String STUDENT_ID = "student-1";
    private static final String TASK_ID = "task-1";
    private static final String SUB_ID = "sub-1";

    private Task taskWith(BigDecimal maxScore) {
        return Task.builder()
                .id(TaskId.of(TASK_ID))
                .subjectId("sub-x")
                .organizationId(ORG_ID)
                .createdBy(PROF_ID)
                .title("T1")
                .description("desc")
                .deadline(LocalDateTime.now().plusDays(1))
                .maxScore(maxScore)
                .status(TaskStatus.PUBLISHED)
                .attachments(List.of())
                .build();
    }

    private TaskSubmission submission(SubmissionStatus status) {
        return TaskSubmission.builder()
                .id(SubmissionId.of(SUB_ID))
                .taskId(TASK_ID)
                .studentId(STUDENT_ID)
                .organizationId(ORG_ID)
                .textResponse("resposta")
                .status(status)
                .attachments(List.of())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void evaluate_success_withGradeAndFeedback() {
        var sub = submission(SubmissionStatus.SUBMITTED);
        var task = taskWith(new BigDecimal("10.00"));
        when(submissionRepository.findById(SubmissionId.of(SUB_ID))).thenReturn(Optional.of(sub));
        when(taskRepository.findByIdAndOrganization(TaskId.of(TASK_ID), ORG_ID)).thenReturn(Optional.of(task));
        when(submissionRepository.save(any())).thenAnswer(inv -> {
            TaskSubmission s = inv.getArgument(0);
            return s;
        });

        var cmd = EvaluateSubmissionCommand.builder()
                .submissionId(SUB_ID).professorId(PROF_ID).organizationId(ORG_ID)
                .grade(new BigDecimal("8.5")).feedback("Ótimo trabalho").build();

        var result = service.execute(cmd);

        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.EVALUATED);
        assertThat(result.getGrade()).isEqualByComparingTo("8.5");
        assertThat(result.getFeedback()).isEqualTo("Ótimo trabalho");
        verify(evaluatedEvent).fire(any(SubmissionEvaluatedEvent.class));
    }

    @Test
    void evaluate_success_feedbackOnly_noMaxScore() {
        var sub = submission(SubmissionStatus.SUBMITTED);
        var task = taskWith(null);
        when(submissionRepository.findById(SubmissionId.of(SUB_ID))).thenReturn(Optional.of(sub));
        when(taskRepository.findByIdAndOrganization(TaskId.of(TASK_ID), ORG_ID)).thenReturn(Optional.of(task));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var cmd = EvaluateSubmissionCommand.builder()
                .submissionId(SUB_ID).professorId(PROF_ID).organizationId(ORG_ID)
                .grade(null).feedback("Revisado").build();

        var result = service.execute(cmd);
        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.EVALUATED);
        assertThat(result.getGrade()).isNull();
    }

    @Test
    void evaluate_throws_whenAlreadyEvaluated() {
        var sub = submission(SubmissionStatus.EVALUATED);
        var task = taskWith(new BigDecimal("10.00"));
        when(submissionRepository.findById(SubmissionId.of(SUB_ID))).thenReturn(Optional.of(sub));
        when(taskRepository.findByIdAndOrganization(TaskId.of(TASK_ID), ORG_ID)).thenReturn(Optional.of(task));

        var cmd = EvaluateSubmissionCommand.builder()
                .submissionId(SUB_ID).professorId(PROF_ID).organizationId(ORG_ID)
                .grade(new BigDecimal("5")).feedback("x").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(SubmissionAlreadyEvaluatedException.class);
    }

    @Test
    void evaluate_throws_whenGradeExceedsMaxScore() {
        var sub = submission(SubmissionStatus.SUBMITTED);
        var task = taskWith(new BigDecimal("10.00"));
        when(submissionRepository.findById(SubmissionId.of(SUB_ID))).thenReturn(Optional.of(sub));
        when(taskRepository.findByIdAndOrganization(TaskId.of(TASK_ID), ORG_ID)).thenReturn(Optional.of(task));

        var cmd = EvaluateSubmissionCommand.builder()
                .submissionId(SUB_ID).professorId(PROF_ID).organizationId(ORG_ID)
                .grade(new BigDecimal("11.00")).feedback("x").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(GradeExceedsMaxScoreException.class);
    }

    @Test
    void evaluate_throws_whenGradeProvidedButNoMaxScore() {
        var sub = submission(SubmissionStatus.SUBMITTED);
        var task = taskWith(null);
        when(submissionRepository.findById(SubmissionId.of(SUB_ID))).thenReturn(Optional.of(sub));
        when(taskRepository.findByIdAndOrganization(TaskId.of(TASK_ID), ORG_ID)).thenReturn(Optional.of(task));

        var cmd = EvaluateSubmissionCommand.builder()
                .submissionId(SUB_ID).professorId(PROF_ID).organizationId(ORG_ID)
                .grade(new BigDecimal("5")).feedback("x").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(GradeNotAllowedException.class);
    }

    @Test
    void evaluate_throws_whenProfessorDoesNotOwnTask() {
        var sub = submission(SubmissionStatus.SUBMITTED);
        var task = Task.builder()
                .id(TaskId.of(TASK_ID)).subjectId("sub-x").organizationId(ORG_ID)
                .createdBy("other-prof").title("T").description("d")
                .deadline(LocalDateTime.now().plusDays(1)).maxScore(new BigDecimal("10"))
                .status(TaskStatus.PUBLISHED).attachments(List.of()).build();
        when(submissionRepository.findById(SubmissionId.of(SUB_ID))).thenReturn(Optional.of(sub));
        when(taskRepository.findByIdAndOrganization(TaskId.of(TASK_ID), ORG_ID)).thenReturn(Optional.of(task));

        var cmd = EvaluateSubmissionCommand.builder()
                .submissionId(SUB_ID).professorId(PROF_ID).organizationId(ORG_ID)
                .grade(new BigDecimal("5")).feedback("x").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(UnauthorizedTaskOperationException.class);
    }

    @Test
    void evaluate_throws_whenSubmissionNotFound() {
        when(submissionRepository.findById(SubmissionId.of(SUB_ID))).thenReturn(Optional.empty());

        var cmd = EvaluateSubmissionCommand.builder()
                .submissionId(SUB_ID).professorId(PROF_ID).organizationId(ORG_ID)
                .grade(null).feedback("x").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(SubmissionNotFoundException.class);
    }
}
