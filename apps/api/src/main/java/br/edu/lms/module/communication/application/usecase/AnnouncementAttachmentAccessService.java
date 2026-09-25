package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.storage.domain.model.FileRequester;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.FileAccessPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

/** Mesma regra do mural (`ListAnnouncementsService`): só quem é membro da turma, em qualquer papel. */
@ApplicationScoped
@RequiredArgsConstructor
public class AnnouncementAttachmentAccessService implements FileAccessPort {

    private final AnnouncementRepository announcementRepository;
    private final ClassroomQueryPort classroomQueryPort;

    @Override
    public StorageContext context() {
        return StorageContext.ANNOUNCEMENT_ATTACHMENT;
    }

    @Override
    public boolean canRead(String fileKey, FileRequester requester) {
        var organizationId = requester.getOrganizationId();
        return announcementRepository.findClassroomIdByAttachmentFileKey(fileKey, organizationId)
                .map(classroomId -> classroomQueryPort.isMember(requester.getUserId(), classroomId, organizationId, null))
                .orElse(false);
    }
}
