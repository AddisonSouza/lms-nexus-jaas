package br.edu.lms.module.communication.domain.port.in;

import br.edu.lms.module.communication.application.dto.AnnouncementResponse;

import java.util.List;

public interface ListAnnouncementsUseCase {
    List<AnnouncementResponse> execute(String classroomId, String userId, String organizationId);
}
