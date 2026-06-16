package br.edu.lms.module.communication.domain.event;

public record AnnouncementPostedEvent(
        String announcementId,
        String classroomId,
        String authorId,
        String organizationId
) {
}
