package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.application.dto.EditAnnouncementCommand;
import br.edu.lms.module.communication.domain.exception.AnnouncementNotFoundException;
import br.edu.lms.module.communication.domain.exception.EmptyContentException;
import br.edu.lms.module.communication.domain.exception.UnauthorizedAnnouncementOperationException;
import br.edu.lms.module.communication.domain.model.Announcement;
import br.edu.lms.module.communication.domain.model.AnnouncementId;
import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditAnnouncementServiceTest {

    @Mock AnnouncementRepository announcementRepository;
    @Mock StoragePort storagePort;
    @InjectMocks EditAnnouncementService service;

    private static final String ORG_ID = "org-1";
    private static final String ANNOUNCEMENT_ID = "ann-1";
    private static final String AUTHOR_ID = "prof-1";

    private Announcement existing(String authorId) {
        return Announcement.builder()
                .id(AnnouncementId.of(ANNOUNCEMENT_ID)).classroomId("classroom-1").organizationId(ORG_ID)
                .authorId(authorId).content("Aviso original").attachments(List.of())
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void edit_success() {
        when(announcementRepository.findById(AnnouncementId.of(ANNOUNCEMENT_ID))).thenReturn(Optional.of(existing(AUTHOR_ID)));
        when(announcementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var cmd = EditAnnouncementCommand.builder()
                .announcementId(ANNOUNCEMENT_ID).userId(AUTHOR_ID).organizationId(ORG_ID)
                .content("Aviso atualizado").build();

        var result = service.execute(cmd);

        assertThat(result.getContent()).isEqualTo("Aviso atualizado");
    }

    @Test
    void edit_throws_whenNotAuthor() {
        when(announcementRepository.findById(AnnouncementId.of(ANNOUNCEMENT_ID))).thenReturn(Optional.of(existing("other-prof")));

        var cmd = EditAnnouncementCommand.builder()
                .announcementId(ANNOUNCEMENT_ID).userId(AUTHOR_ID).organizationId(ORG_ID)
                .content("Tentativa de edição").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(UnauthorizedAnnouncementOperationException.class);
    }

    @Test
    void edit_throws_whenAnnouncementNotFound() {
        when(announcementRepository.findById(AnnouncementId.of(ANNOUNCEMENT_ID))).thenReturn(Optional.empty());

        var cmd = EditAnnouncementCommand.builder()
                .announcementId(ANNOUNCEMENT_ID).userId(AUTHOR_ID).organizationId(ORG_ID)
                .content("Aviso").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(AnnouncementNotFoundException.class);
    }

    @Test
    void edit_throws_whenContentBlank() {
        when(announcementRepository.findById(AnnouncementId.of(ANNOUNCEMENT_ID))).thenReturn(Optional.of(existing(AUTHOR_ID)));

        var cmd = EditAnnouncementCommand.builder()
                .announcementId(ANNOUNCEMENT_ID).userId(AUTHOR_ID).organizationId(ORG_ID)
                .content("").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(EmptyContentException.class);
    }
}
