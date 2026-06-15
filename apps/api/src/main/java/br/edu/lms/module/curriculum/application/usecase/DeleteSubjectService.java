package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.DeleteSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class DeleteSubjectService implements DeleteSubjectUseCase {

    private final SubjectRepository subjectRepository;

    @Override
    public void execute(SubjectId id, String organizationId) {
        subjectRepository.findById(id, organizationId)
                .orElseThrow(SubjectNotFoundException::new);
        subjectRepository.softDelete(id, organizationId);
        log.info("Subject soft-deleted: {} in org: {}", id.getValue(), organizationId);
    }
}
