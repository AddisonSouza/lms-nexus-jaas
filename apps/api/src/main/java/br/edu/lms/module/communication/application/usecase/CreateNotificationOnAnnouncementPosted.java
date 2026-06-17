package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.event.AnnouncementPostedEvent;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationId;
import br.edu.lms.module.communication.domain.model.NotificationType;
import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CreateNotificationOnAnnouncementPosted {

    private final ClassroomQueryPort classroomQueryPort;
    private final NotificationRepository notificationRepository;
    private final NotificationUnreadCounterPort notificationUnreadCounterPort;

    void onAnnouncementPosted(@Observes AnnouncementPostedEvent event) {
        var recipients = classroomQueryPort.listMemberUserIds(event.classroomId(), "ALUNO").stream()
                .filter(userId -> !userId.equals(event.authorId()))
                .toList();

        for (String userId : recipients) {
            var notification = Notification.builder()
                    .id(NotificationId.generate())
                    .userId(userId)
                    .organizationId(event.organizationId())
                    .type(NotificationType.ANNOUNCEMENT_POSTED)
                    .referenceId(event.announcementId())
                    .title("Novo aviso")
                    .message("Um novo aviso foi publicado na turma.")
                    .actionLink("/classrooms/" + event.classroomId())
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
            notificationUnreadCounterPort.increment(userId);
        }

        log.debug("Created {} notifications for AnnouncementPostedEvent classroom={}", recipients.size(), event.classroomId());
    }
}
