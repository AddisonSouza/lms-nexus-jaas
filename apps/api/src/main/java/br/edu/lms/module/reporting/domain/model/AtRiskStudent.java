package br.edu.lms.module.reporting.domain.model;

import lombok.Value;

@Value
public class AtRiskStudent {
    String studentId;
    String studentName;
    long pendingCount;
}
