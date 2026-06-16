package br.edu.lms.module.assessment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EvaluateSubmissionCommand {
    private String submissionId;
    private String professorId;
    private String organizationId;
    private BigDecimal grade;
    private String feedback;
}
