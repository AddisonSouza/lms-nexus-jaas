package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.AttachmentInput;
import br.edu.lms.module.assessment.application.dto.CreateTaskCommand;
import br.edu.lms.module.assessment.domain.exception.DeadlineNotInFutureException;
import br.edu.lms.module.assessment.domain.exception.InvalidAttachmentTypeException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskStatus;
import br.edu.lms.module.assessment.domain.port.out.SubjectQueryPort;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.model.StoredFile;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock SubjectQueryPort subjectQueryPort;
    @Mock StoragePort storagePort;

    @InjectMocks CreateTaskService sut;

    private CreateTaskCommand.CreateTaskCommandBuilder baseCommand() {
        return CreateTaskCommand.builder()
                .subjectId("sub-1")
                .organizationId("org-1")
                .createdBy("user-1")
                .title("Tarefa 1")
                .description("Enunciado")
                .deadline(LocalDateTime.now().plusDays(7));
    }

    @Test
    void shouldCreateDraftTaskWithoutAttachments() {
        when(subjectQueryPort.existsByIdAndTeacher("sub-1", "org-1", "user-1")).thenReturn(true);
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(baseCommand().build());

        assertThat(result.getStatus()).isEqualTo(TaskStatus.DRAFT);
        assertThat(result.getTitle()).isEqualTo("Tarefa 1");
        assertThat(result.getAttachments()).isEmpty();
        verifyNoInteractions(storagePort);
    }

    @Test
    void shouldUploadAttachmentsAndStoreKeys() {
        when(subjectQueryPort.existsByIdAndTeacher("sub-1", "org-1", "user-1")).thenReturn(true);
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(storagePort.store(any(), any(), any(), anyLong(), eq(StorageContext.TASK_ATTACHMENT)))
                .thenReturn(new StoredFile("task_attachment/2025/01/uuid.pdf", "file.pdf", "application/pdf", 1024));

        var attachment = new AttachmentInput(new ByteArrayInputStream(new byte[]{1}), "file.pdf", "application/pdf", 1024);
        var result = sut.execute(baseCommand().attachments(List.of(attachment)).build());

        assertThat(result.getAttachments()).hasSize(1);
        assertThat(result.getAttachments().get(0).getFileKey()).isEqualTo("task_attachment/2025/01/uuid.pdf");
    }

    @Test
    void shouldRejectPastDeadline() {
        when(subjectQueryPort.existsByIdAndTeacher("sub-1", "org-1", "user-1")).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(baseCommand().deadline(LocalDateTime.now().minusHours(1)).build()))
                .isInstanceOf(DeadlineNotInFutureException.class)
                .hasMessageContaining("future");
    }

    @Test
    void shouldRejectDisallowedMimeType() {
        when(subjectQueryPort.existsByIdAndTeacher("sub-1", "org-1", "user-1")).thenReturn(true);

        var attachment = new AttachmentInput(new ByteArrayInputStream(new byte[]{1}), "virus.exe", "application/x-msdownload", 100);
        assertThatThrownBy(() -> sut.execute(baseCommand().attachments(List.of(attachment)).build()))
                .isInstanceOf(InvalidAttachmentTypeException.class);
    }

    @Test
    void shouldRejectTeacherNotLinkedToSubject() {
        when(subjectQueryPort.existsByIdAndTeacher("sub-1", "org-1", "user-1")).thenReturn(false);

        assertThatThrownBy(() -> sut.execute(baseCommand().build()))
                .isInstanceOf(UnauthorizedTaskOperationException.class);
    }
}
