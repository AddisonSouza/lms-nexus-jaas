package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.exception.AnnouncementNotFoundException;
import br.edu.lms.module.communication.domain.exception.UnauthorizedAnnouncementOperationException;
import br.edu.lms.module.communication.domain.model.Announcement;
import br.edu.lms.module.communication.domain.model.AnnouncementId;
import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAnnouncementServiceTest {

    @Mock AnnouncementRepository announcementRepository;
    @InjectMocks DeleteAnnouncementService service;

    private static final String ORG_ID = "org-1";
    private static final String ANNOUNCEMENT_ID = "ann-1";
    private static final String AUTHOR_ID = "prof-1";

    private Announcement existing(String authorId) {
        return Announcement.builder()
                .id(AnnouncementId.of(ANNOUNCEMENT_ID)).classroomId("classroom-1").organizationId(ORG_ID)
                .authorId(authorId).content("Aviso").attachments(List.of())
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void delete_success_setsDeletedAt() {
        when(announcementRepository.findById(AnnouncementId.of(ANNOUNCEMENT_ID))).thenReturn(Optional.of(existing(AUTHOR_ID)));

        service.execute(ANNOUNCEMENT_ID, AUTHOR_ID, ORG_ID);

        var captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    void delete_throws_whenNotAuthor() {
        when(announcementRepository.findById(AnnouncementId.of(ANNOUNCEMENT_ID))).thenReturn(Optional.of(existing("other-prof")));

        assertThatThrownBy(() -> service.execute(ANNOUNCEMENT_ID, AUTHOR_ID, ORG_ID))
                .isInstanceOf(UnauthorizedAnnouncementOperationException.class);
    }

    @Test
    void delete_throws_whenNotFound() {
        when(announcementRepository.findById(AnnouncementId.of(ANNOUNCEMENT_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(ANNOUNCEMENT_ID, AUTHOR_ID, ORG_ID))
                .isInstanceOf(AnnouncementNotFoundException.class);
    }
}
