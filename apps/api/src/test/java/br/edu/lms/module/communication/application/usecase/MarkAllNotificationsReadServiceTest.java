package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MarkAllNotificationsReadServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock NotificationUnreadCounterPort notificationUnreadCounterPort;
    @InjectMocks MarkAllNotificationsReadService service;

    private static final String ORG_ID = "org-1";
    private static final String USER_ID = "user-1";

    @Test
    void execute_marksAllReadAndResetsCounter() {
        service.execute(USER_ID, ORG_ID);

        verify(notificationRepository, times(1)).markAllReadByUser(USER_ID, ORG_ID);
        verify(notificationUnreadCounterPort, times(1)).reset(USER_ID);
    }

    @Test
    void execute_noUnreadNotifications_stillResetsCounterWithoutError() {
        service.execute(USER_ID, ORG_ID);

        verify(notificationRepository, times(1)).markAllReadByUser(USER_ID, ORG_ID);
        verify(notificationUnreadCounterPort, times(1)).reset(USER_ID);
    }
}
