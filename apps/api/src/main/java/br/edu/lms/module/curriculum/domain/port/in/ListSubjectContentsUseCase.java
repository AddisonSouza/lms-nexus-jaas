package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.SubjectContentsGroupedResponse;

public interface ListSubjectContentsUseCase {
    SubjectContentsGroupedResponse execute(String subjectId, String organizationId, String requestingUserId, String requestingUserRole);
}
