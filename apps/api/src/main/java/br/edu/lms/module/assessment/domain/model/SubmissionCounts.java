package br.edu.lms.module.assessment.domain.model;

/**
 * Quantas respostas uma tarefa recebeu e quantas ainda esperam avaliação.
 * Pendente é o que está em SUBMITTED — o enum do módulo só tem SUBMITTED e
 * EVALUATED.
 */
public record SubmissionCounts(long total, long pendingEvaluation) {

    public static SubmissionCounts none() {
        return new SubmissionCounts(0, 0);
    }
}
