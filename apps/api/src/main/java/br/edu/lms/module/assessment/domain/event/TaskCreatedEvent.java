package br.edu.lms.module.assessment.domain.event;

public record TaskCreatedEvent(String taskId, String subjectId, String organizationId) {
}
