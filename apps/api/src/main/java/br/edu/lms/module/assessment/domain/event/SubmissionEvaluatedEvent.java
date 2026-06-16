package br.edu.lms.module.assessment.domain.event;

public record SubmissionEvaluatedEvent(String submissionId, String taskId, String studentId, String organizationId) {
}
