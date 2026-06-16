package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;
import java.math.BigDecimal;

public class GradeExceedsMaxScoreException extends RuntimeException implements HttpMappable {
    public GradeExceedsMaxScoreException(BigDecimal grade, BigDecimal maxScore) {
        super("Grade " + grade + " exceeds maximum score " + maxScore);
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "GRADE_EXCEEDS_MAX_SCORE"; }
}
