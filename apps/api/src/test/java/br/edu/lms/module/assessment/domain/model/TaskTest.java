package br.edu.lms.module.assessment.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

    private Task task(TaskStatus status, LocalDateTime deadline) {
        return Task.builder()
                .id(TaskId.of("task-1"))
                .subjectId("sub-1")
                .organizationId("org-1")
                .createdBy("prof-1")
                .title("Tarefa")
                .description("Enunciado")
                .deadline(deadline)
                .maxScore(BigDecimal.TEN)
                .status(status)
                .attachments(List.of())
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();
    }

    @Test
    void publishedTaskPastItsDeadlineReadsAsClosed() {
        Task sut = task(TaskStatus.PUBLISHED, LocalDateTime.now().minusMinutes(1));

        assertThat(sut.effectiveStatus()).isEqualTo(TaskStatus.CLOSED);
        assertThat(sut.getStatus()).isEqualTo(TaskStatus.PUBLISHED);
    }

    @Test
    void publishedTaskWithinItsDeadlineStaysPublished() {
        Task sut = task(TaskStatus.PUBLISHED, LocalDateTime.now().plusDays(1));

        assertThat(sut.effectiveStatus()).isEqualTo(TaskStatus.PUBLISHED);
    }

    @Test
    void draftTaskIsNeverClosedByTheDeadline() {
        Task sut = task(TaskStatus.DRAFT, LocalDateTime.now().minusDays(3));

        assertThat(sut.effectiveStatus()).isEqualTo(TaskStatus.DRAFT);
    }

    @Test
    void gradedTaskKeepsItsStatusPastTheDeadline() {
        Task sut = task(TaskStatus.GRADED, LocalDateTime.now().minusDays(3));

        assertThat(sut.effectiveStatus()).isEqualTo(TaskStatus.GRADED);
    }

    @Test
    void closedTaskStaysClosed() {
        Task sut = task(TaskStatus.CLOSED, LocalDateTime.now().plusDays(1));

        assertThat(sut.effectiveStatus()).isEqualTo(TaskStatus.CLOSED);
    }

    // Defensivo: o domínio não exige deadline no builder.
    @Test
    void taskWithoutDeadlineKeepsItsStatus() {
        Task sut = task(TaskStatus.PUBLISHED, null);

        assertThat(sut.effectiveStatus()).isEqualTo(TaskStatus.PUBLISHED);
    }
}
