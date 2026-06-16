package br.edu.lms.module.communication.domain.port.out;

import br.edu.lms.module.communication.domain.model.Announcement;
import br.edu.lms.module.communication.domain.model.AnnouncementId;

import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository {
    Announcement save(Announcement announcement);
    Optional<Announcement> findById(AnnouncementId id);
    List<Announcement> findByClassroomOrderByCreatedAtDesc(String classroomId, String organizationId);
}
