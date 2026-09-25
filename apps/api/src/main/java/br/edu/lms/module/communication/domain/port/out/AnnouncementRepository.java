package br.edu.lms.module.communication.domain.port.out;

import br.edu.lms.module.communication.domain.model.Announcement;
import br.edu.lms.module.communication.domain.model.AnnouncementId;

import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository {
    Announcement save(Announcement announcement);
    Optional<Announcement> findById(AnnouncementId id, String organizationId);
    List<Announcement> findByClassroomOrderByCreatedAtDesc(String classroomId, String organizationId);
    /** Turma do aviso ativo da organização que tem este arquivo entre os anexos. */
    Optional<String> findClassroomIdByAttachmentFileKey(String fileKey, String organizationId);
}
