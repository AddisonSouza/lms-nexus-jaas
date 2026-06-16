package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.assessment.domain.event.SubmissionEvaluatedEvent;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationId;
import br.edu.lms.module.communication.domain.model.NotificationType;
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
public class CreateNotificationOnSubmissionEvaluated {

    private final NotificationRepository notificationRepository;
    private final NotificationUnreadCounterPort notificationUnreadCounterPort;

    void onSubmissionEvaluated(@Observes SubmissionEvaluatedEvent event) {
        var notification = Notification.builder()
                .id(NotificationId.generate())
                .userId(event.studentId())
                .organizationId(event.organizationId())
                .type(NotificationType.SUBMISSION_EVALUATED)
                .referenceId(event.taskId())
                .title("Avaliação disponível")
                .message("Sua resposta foi avaliada.")
                .actionLink("/assessment/student-tasks?taskId=" + event.taskId())
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        notificationUnreadCounterPort.increment(event.studentId());

        log.debug("Created notification for SubmissionEvaluatedEvent task={} student={}", event.taskId(), event.studentId());
    }
}
