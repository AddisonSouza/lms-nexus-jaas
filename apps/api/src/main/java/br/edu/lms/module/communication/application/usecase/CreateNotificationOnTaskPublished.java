package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.assessment.domain.event.TaskPublishedEvent;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationId;
import br.edu.lms.module.communication.domain.model.NotificationType;
import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import br.edu.lms.module.communication.domain.port.out.SubjectQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CreateNotificationOnTaskPublished {

    private final SubjectQueryPort subjectQueryPort;
    private final ClassroomQueryPort classroomQueryPort;
    private final NotificationRepository notificationRepository;
    private final NotificationUnreadCounterPort notificationUnreadCounterPort;

    void onTaskPublished(@Observes TaskPublishedEvent event) {
        var classroomIds = subjectQueryPort.findClassroomIdsBySubject(event.subjectId());

        var recipients = new LinkedHashSet<String>();
        for (String classroomId : classroomIds) {
            recipients.addAll(classroomQueryPort.listMemberUserIds(classroomId, "ALUNO"));
        }

        for (String userId : recipients) {
            var notification = Notification.builder()
                    .id(NotificationId.generate())
                    .userId(userId)
                    .organizationId(event.organizationId())
                    .type(NotificationType.TASK_PUBLISHED)
                    .referenceId(event.taskId())
                    .title("Nova tarefa")
                    .message("Uma nova tarefa foi publicada.")
                    .actionLink("/assessment/student-tasks?taskId=" + event.taskId())
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
            notificationUnreadCounterPort.increment(userId);
        }

        log.debug("Created {} notifications for TaskPublishedEvent task={}", recipients.size(), event.taskId());
    }
}
