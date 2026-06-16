package br.edu.lms.module.assessment.domain.event;

public record TaskSubmittedEvent(String submissionId, String taskId, String studentId, String organizationId) {
}
