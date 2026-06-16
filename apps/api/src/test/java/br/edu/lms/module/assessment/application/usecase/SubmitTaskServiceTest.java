package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.SubmitTaskCommand;
import br.edu.lms.module.assessment.domain.event.TaskSubmittedEvent;
import br.edu.lms.module.assessment.domain.exception.DeadlineExpiredException;
import br.edu.lms.module.assessment.domain.exception.EmptySubmissionException;
import br.edu.lms.module.assessment.domain.exception.InvalidTaskStateException;
import br.edu.lms.module.assessment.domain.exception.SubmissionAlreadyExistsException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitTaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock SubmissionRepository submissionRepository;
    @Mock StoragePort storagePort;
    @Mock Event<TaskSubmittedEvent> submittedEvent;

    @InjectMocks SubmitTaskService sut;

    private Task publishedTask(LocalDateTime deadline) {
        return Task.builder()
                .id(TaskId.of("task-1"))
                .subjectId("sub-1")
                .organizationId("org-1")
                .createdBy("professor-1")
                .title("Tarefa")
                .description("Enunciado")
                .deadline(deadline)
                .status(TaskStatus.PUBLISHED)
                .attachments(List.of())
                .build();
    }

    private TaskSubmission savedSubmission() {
        return TaskSubmission.builder()
                .id(SubmissionId.generate())
                .taskId("task-1")
                .studentId("student-1")
                .organizationId("org-1")
                .textResponse("Minha resposta")
                .status(SubmissionStatus.SUBMITTED)
                .attachments(List.of())
                .build();
    }

    private SubmitTaskCommand commandWithText() {
        return SubmitTaskCommand.builder()
                .taskId("task-1")
                .studentId("student-1")
                .organizationId("org-1")
                .textResponse("Minha resposta")
                .attachments(List.of())
                .build();
    }

    @Test
    void shouldSubmitSuccessfullyWithText() {
        when(taskRepository.findByIdAndOrganization(TaskId.of("task-1"), "org-1"))
                .thenReturn(Optional.of(publishedTask(LocalDateTime.now().plusDays(1))));
        when(submissionRepository.findByTaskAndStudent("task-1", "student-1")).thenReturn(Optional.empty());
        when(submissionRepository.save(any())).thenReturn(savedSubmission());

        var result = sut.execute(commandWithText());

        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        verify(submittedEvent).fire(any(TaskSubmittedEvent.class));
    }

    @Test
    void shouldThrowWhenDeadlineExpired() {
        when(taskRepository.findByIdAndOrganization(TaskId.of("task-1"), "org-1"))
                .thenReturn(Optional.of(publishedTask(LocalDateTime.now().minusMinutes(1))));

        assertThatThrownBy(() -> sut.execute(commandWithText()))
                .isInstanceOf(DeadlineExpiredException.class);
    }

    @Test
    void shouldThrowWhenTaskNotPublished() {
        var draftTask = publishedTask(LocalDateTime.now().plusDays(1)).toBuilder()
                .status(TaskStatus.DRAFT).build();
        when(taskRepository.findByIdAndOrganization(TaskId.of("task-1"), "org-1"))
                .thenReturn(Optional.of(draftTask));

        assertThatThrownBy(() -> sut.execute(commandWithText()))
                .isInstanceOf(InvalidTaskStateException.class);
    }

    @Test
    void shouldThrowWhenSubmissionAlreadyExists() {
        when(taskRepository.findByIdAndOrganization(TaskId.of("task-1"), "org-1"))
                .thenReturn(Optional.of(publishedTask(LocalDateTime.now().plusDays(1))));
        when(submissionRepository.findByTaskAndStudent("task-1", "student-1"))
                .thenReturn(Optional.of(savedSubmission()));

        assertThatThrownBy(() -> sut.execute(commandWithText()))
                .isInstanceOf(SubmissionAlreadyExistsException.class);
    }

    @Test
    void shouldThrowWhenNoTextNorFiles() {
        when(taskRepository.findByIdAndOrganization(TaskId.of("task-1"), "org-1"))
                .thenReturn(Optional.of(publishedTask(LocalDateTime.now().plusDays(1))));

        var command = SubmitTaskCommand.builder()
                .taskId("task-1")
                .studentId("student-1")
                .organizationId("org-1")
                .textResponse(null)
                .attachments(List.of())
                .build();

        assertThatThrownBy(() -> sut.execute(command))
                .isInstanceOf(EmptySubmissionException.class);
    }
}
