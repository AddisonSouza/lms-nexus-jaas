package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.EditSubmissionCommand;
import br.edu.lms.module.assessment.domain.event.TaskSubmittedEvent;
import br.edu.lms.module.assessment.domain.exception.DeadlineExpiredException;
import br.edu.lms.module.assessment.domain.exception.SubmissionAlreadyEvaluatedException;
import br.edu.lms.module.assessment.domain.exception.SubmissionNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.SubmissionId;
import br.edu.lms.module.assessment.domain.model.SubmissionStatus;
import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.model.TaskStatus;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditSubmissionServiceTest {

    @Mock SubmissionRepository submissionRepository;
    @Mock TaskRepository taskRepository;
    @Mock StoragePort storagePort;
    @Mock Event<TaskSubmittedEvent> submittedEvent;

    @InjectMocks EditSubmissionService sut;

    private TaskSubmission submission(String studentId, SubmissionStatus status) {
        return TaskSubmission.builder()
                .id(SubmissionId.of("sub-1"))
                .taskId("task-1")
                .studentId(studentId)
                .organizationId("org-1")
                .textResponse("Resposta original")
                .status(status)
                .attachments(List.of())
                .build();
    }

    private Task publishedTask(LocalDateTime deadline) {
        return Task.builder()
                .id(TaskId.of("task-1"))
                .subjectId("sub-1")
                .organizationId("org-1")
                .createdBy("professor-1")
                .title("Tarefa")
                .description("Desc")
                .deadline(deadline)
                .status(TaskStatus.PUBLISHED)
                .attachments(List.of())
                .build();
    }

    private EditSubmissionCommand command(String studentId) {
        return EditSubmissionCommand.builder()
                .submissionId("sub-1")
                .taskId("task-1")
                .studentId(studentId)
                .textResponse("Resposta atualizada")
                .attachments(List.of())
                .build();
    }

    @Test
    void shouldEditSuccessfully() {
        when(submissionRepository.findById(SubmissionId.of("sub-1")))
                .thenReturn(Optional.of(submission("student-1", SubmissionStatus.SUBMITTED)));
        when(taskRepository.findById(TaskId.of("task-1")))
                .thenReturn(Optional.of(publishedTask(LocalDateTime.now().plusDays(1))));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(command("student-1"));

        assertThat(result.getTextResponse()).isEqualTo("Resposta atualizada");
        verify(submittedEvent).fire(any(TaskSubmittedEvent.class));
    }

    @Test
    void shouldThrowWhenOwnershipViolated() {
        when(submissionRepository.findById(SubmissionId.of("sub-1")))
                .thenReturn(Optional.of(submission("student-1", SubmissionStatus.SUBMITTED)));

        assertThatThrownBy(() -> sut.execute(command("other-student")))
                .isInstanceOf(UnauthorizedTaskOperationException.class);
    }

    @Test
    void shouldThrowWhenDeadlineExpired() {
        when(submissionRepository.findById(SubmissionId.of("sub-1")))
                .thenReturn(Optional.of(submission("student-1", SubmissionStatus.SUBMITTED)));
        when(taskRepository.findById(TaskId.of("task-1")))
                .thenReturn(Optional.of(publishedTask(LocalDateTime.now().minusMinutes(1))));

        assertThatThrownBy(() -> sut.execute(command("student-1")))
                .isInstanceOf(DeadlineExpiredException.class);
    }

    @Test
    void shouldThrowWhenAlreadyEvaluated() {
        when(submissionRepository.findById(SubmissionId.of("sub-1")))
                .thenReturn(Optional.of(submission("student-1", SubmissionStatus.EVALUATED)));

        assertThatThrownBy(() -> sut.execute(command("student-1")))
                .isInstanceOf(SubmissionAlreadyEvaluatedException.class);
    }

    @Test
    void shouldThrowWhenSubmissionNotFound() {
        when(submissionRepository.findById(SubmissionId.of("sub-1"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(command("student-1")))
                .isInstanceOf(SubmissionNotFoundException.class);
    }
}
