package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import br.edu.lms.module.storage.domain.model.FileRequester;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.FileAccessPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

/** Mesma regra da listagem de conteúdos (`ListSubjectContentsService`): o aluno precisa estar numa turma da disciplina. */
@ApplicationScoped
@RequiredArgsConstructor
public class LessonMaterialAccessService implements FileAccessPort {

    private final ContentRepository contentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomQueryPort classroomQueryPort;

    @Override
    public StorageContext context() {
        return StorageContext.LESSON_MATERIAL;
    }

    @Override
    public boolean canRead(String fileKey, FileRequester requester) {
        var organizationId = requester.getOrganizationId();
        return contentRepository.findSubjectIdByFileKey(fileKey, organizationId)
                .map(subjectId -> !"ALUNO".equals(requester.getRole())
                        || classroomQueryPort.isMemberOfAnyClassroom(
                                requester.getUserId(),
                                subjectRepository.findClassroomIdsBySubject(subjectId),
                                organizationId))
                .orElse(false);
    }
}
