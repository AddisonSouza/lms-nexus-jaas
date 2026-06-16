package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.domain.event.TaskCreatedEvent;
import br.edu.lms.module.assessment.domain.exception.InvalidTaskStateException;
import br.edu.lms.module.assessment.domain.exception.TaskNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.model.TaskStatus;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishTaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock Event<TaskCreatedEvent> taskCreatedEvent;

    @InjectMocks PublishTaskService sut;

    private Task draftTask(String createdBy) {
        return Task.builder()
                .id(TaskId.of("task-1"))
                .subjectId("sub-1")
                .organizationId("org-1")
                .createdBy(createdBy)
                .title("Tarefa")
                .description("Enunciado")
                .deadline(LocalDateTime.now().plusDays(3))
                .status(TaskStatus.DRAFT)
                .attachments(java.util.List.of())
                .build();
    }

    @Test
    void shouldPublishDraftTask() {
        var task = draftTask("user-1");
        when(taskRepository.findByIdAndOrganization(TaskId.of("task-1"), "org-1")).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute("task-1", "org-1", "user-1");

        assertThat(result.getStatus()).isEqualTo(TaskStatus.PUBLISHED);
        verify(taskCreatedEvent).fire(any(TaskCreatedEvent.class));
    }

    @Test
    void shouldRejectPublishIfAlreadyPublished() {
        var task = draftTask("user-1").toBuilder().status(TaskStatus.PUBLISHED).build();
        when(taskRepository.findByIdAndOrganization(TaskId.of("task-1"), "org-1")).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> sut.execute("task-1", "org-1", "user-1"))
                .isInstanceOf(InvalidTaskStateException.class);
    }

    @Test
    void shouldRejectPublishIfNotAuthor() {
        var task = draftTask("user-1");
        when(taskRepository.findByIdAndOrganization(TaskId.of("task-1"), "org-1")).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> sut.execute("task-1", "org-1", "another-user"))
                .isInstanceOf(UnauthorizedTaskOperationException.class);
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findByIdAndOrganization(TaskId.of("bad"), "org-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute("bad", "org-1", "user-1"))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
