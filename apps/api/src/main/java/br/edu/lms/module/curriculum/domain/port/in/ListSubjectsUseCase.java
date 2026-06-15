package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.SubjectResponse;

import java.util.List;

public interface ListSubjectsUseCase {
    List<SubjectResponse> execute(String organizationId);
}
