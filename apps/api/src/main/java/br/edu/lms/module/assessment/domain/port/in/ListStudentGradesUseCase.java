package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.TaskWithGradeResponse;

import java.util.List;

public interface ListStudentGradesUseCase {
    List<TaskWithGradeResponse> execute(String studentId, String organizationId);
}
