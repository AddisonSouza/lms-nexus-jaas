package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.assessment.domain.event.TaskSubmittedEvent;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationId;
import br.edu.lms.module.communication.domain.model.NotificationType;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import br.edu.lms.module.communication.domain.port.out.SubjectQueryPort;
import br.edu.lms.module.communication.domain.port.out.TaskQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CreateNotificationOnTaskSubmitted {

    private final TaskQueryPort taskQueryPort;
    private final SubjectQueryPort subjectQueryPort;
    private final NotificationRepository notificationRepository;
    private final NotificationUnreadCounterPort notificationUnreadCounterPort;

    void onTaskSubmitted(@Observes TaskSubmittedEvent event) {
        var subjectId = taskQueryPort.findSubjectIdByTask(event.taskId());
        if (subjectId.isEmpty()) {
            log.warn("No subject found for taskId={}, skipping notification for TaskSubmittedEvent", event.taskId());
            return;
        }

        List<String> recipients = subjectQueryPort.findTeacherUserIdsBySubject(subjectId.get());

        for (String userId : recipients) {
            var notification = Notification.builder()
                    .id(NotificationId.generate())
                    .userId(userId)
                    .organizationId(event.organizationId())
                    .type(NotificationType.TASK_SUBMITTED)
                    .referenceId(event.taskId())
                    .title("Nova resposta")
                    .message("Um aluno enviou uma resposta para avaliação.")
                    .actionLink("/assessment/tasks?taskId=" + event.taskId())
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
            notificationUnreadCounterPort.increment(userId);
        }

        log.debug("Created {} notifications for TaskSubmittedEvent task={}", recipients.size(), event.taskId());
    }
}
