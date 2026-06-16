package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.exception.UnauthorizedAnnouncementOperationException;
import br.edu.lms.module.communication.domain.model.Announcement;
import br.edu.lms.module.communication.domain.model.AnnouncementId;
import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAnnouncementsServiceTest {

    @Mock AnnouncementRepository announcementRepository;
    @Mock ClassroomQueryPort classroomQueryPort;
    @InjectMocks ListAnnouncementsService service;

    private static final String ORG_ID = "org-1";
    private static final String CLASSROOM_ID = "classroom-1";
    private static final String USER_ID = "user-1";

    @Test
    void list_success() {
        when(classroomQueryPort.isMember(USER_ID, CLASSROOM_ID, ORG_ID, null)).thenReturn(true);
        var announcement = Announcement.builder()
                .id(AnnouncementId.generate()).classroomId(CLASSROOM_ID).organizationId(ORG_ID)
                .authorId("prof-1").content("Aviso").attachments(List.of())
                .createdAt(LocalDateTime.now()).build();
        when(announcementRepository.findByClassroomOrderByCreatedAtDesc(CLASSROOM_ID, ORG_ID))
                .thenReturn(List.of(announcement));

        var result = service.execute(CLASSROOM_ID, USER_ID, ORG_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Aviso");
    }

    @Test
    void list_success_empty() {
        when(classroomQueryPort.isMember(USER_ID, CLASSROOM_ID, ORG_ID, null)).thenReturn(true);
        when(announcementRepository.findByClassroomOrderByCreatedAtDesc(CLASSROOM_ID, ORG_ID))
                .thenReturn(List.of());

        var result = service.execute(CLASSROOM_ID, USER_ID, ORG_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void list_throws_whenUserNotMember() {
        when(classroomQueryPort.isMember(USER_ID, CLASSROOM_ID, ORG_ID, null)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(CLASSROOM_ID, USER_ID, ORG_ID))
                .isInstanceOf(UnauthorizedAnnouncementOperationException.class);
    }
}
