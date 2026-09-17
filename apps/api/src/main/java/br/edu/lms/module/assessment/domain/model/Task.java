package br.edu.lms.module.assessment.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    @EqualsAndHashCode.Include
    private final TaskId id;

    private final String subjectId;
    private final String organizationId;
    private final String createdBy;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private BigDecimal maxScore;
    private TaskStatus status;
    private List<TaskAttachment> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Task publish() {
        if (this.status != TaskStatus.DRAFT) {
            throw new br.edu.lms.module.assessment.domain.exception.InvalidTaskStateException(this.status, TaskStatus.PUBLISHED);
        }
        return this.toBuilder().status(TaskStatus.PUBLISHED).build();
    }

    /**
     * Status como o usuário deve ver: uma tarefa publicada cujo prazo já passou
     * está encerrada, mesmo que o banco ainda guarde PUBLISHED. Derivar na
     * leitura evita job agendado e escrita em GET — e mantém o prazo como única
     * fonte da verdade.
     */
    public TaskStatus effectiveStatus() {
        if (this.status == TaskStatus.PUBLISHED
                && this.deadline != null
                && this.deadline.isBefore(LocalDateTime.now())) {
            return TaskStatus.CLOSED;
        }
        return this.status;
    }

    public Task close() {
        if (this.status != TaskStatus.PUBLISHED) {
            throw new br.edu.lms.module.assessment.domain.exception.InvalidTaskStateException(this.status, TaskStatus.CLOSED);
        }
        return this.toBuilder().status(TaskStatus.CLOSED).build();
    }
}
