package br.edu.lms.module.assessment.domain.event;

public record TaskPublishedEvent(String taskId, String subjectId, String organizationId) {}
