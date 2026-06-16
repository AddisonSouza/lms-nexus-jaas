package br.edu.lms.module.assessment.domain.exception;

import java.math.BigDecimal;

public class GradeExceedsMaxScoreException extends RuntimeException {
    public GradeExceedsMaxScoreException(BigDecimal grade, BigDecimal maxScore) {
        super("Grade " + grade + " exceeds maximum score " + maxScore);
    }
}
