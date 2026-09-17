package br.edu.lms.module.assessment.application.dto;

import br.edu.lms.module.assessment.domain.model.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A tarefa como o professor a vê na listagem: tudo de {@link TaskResponse} mais
 * os contadores de entrega. Existe separado porque {@code TaskResponse} também
 * serve {@code GET /tasks/published}, que o aluno consome — expor ali quantas
 * respostas a turma enviou seria vazamento.
 */
@Getter
@Builder
public class TaskSummaryResponse {
    private String id;
    private String subjectId;
    private String organizationId;
    private String createdBy;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private BigDecimal maxScore;
    private TaskStatus status;
    private List<TaskAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Respostas recebidas. */
    private long submissionCount;

    /** Respostas ainda em SUBMITTED, esperando avaliação. */
    private long pendingEvaluationCount;
}
