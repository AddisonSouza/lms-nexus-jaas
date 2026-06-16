package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.application.dto.PostAnnouncementCommand;
import br.edu.lms.module.communication.domain.event.AnnouncementPostedEvent;
import br.edu.lms.module.communication.domain.exception.EmptyContentException;
import br.edu.lms.module.communication.domain.exception.UnauthorizedAnnouncementOperationException;
import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostAnnouncementServiceTest {

    @Mock AnnouncementRepository announcementRepository;
    @Mock ClassroomQueryPort classroomQueryPort;
    @Mock StoragePort storagePort;
    @Mock Event<AnnouncementPostedEvent> postedEvent;
    @InjectMocks PostAnnouncementService service;

    private static final String ORG_ID = "org-1";
    private static final String CLASSROOM_ID = "classroom-1";
    private static final String PROF_ID = "prof-1";

    @Test
    void post_success_textOnly() {
        when(classroomQueryPort.isMember(PROF_ID, CLASSROOM_ID, ORG_ID, "PROFESSOR")).thenReturn(true);
        when(announcementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var cmd = PostAnnouncementCommand.builder()
                .classroomId(CLASSROOM_ID).organizationId(ORG_ID).authorId(PROF_ID)
                .content("Aviso importante").build();

        var result = service.execute(cmd);

        assertThat(result.getContent()).isEqualTo("Aviso importante");
        assertThat(result.getAttachments()).isEmpty();
        verify(postedEvent).fire(any(AnnouncementPostedEvent.class));
    }

    @Test
    void post_success_withLinkAttachment() {
        when(classroomQueryPort.isMember(PROF_ID, CLASSROOM_ID, ORG_ID, "PROFESSOR")).thenReturn(true);
        when(announcementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var attachment = new br.edu.lms.module.communication.application.dto.AttachmentInput(
                null, null, null, null, "https://example.com", "Material extra");

        var cmd = PostAnnouncementCommand.builder()
                .classroomId(CLASSROOM_ID).organizationId(ORG_ID).authorId(PROF_ID)
                .content("Aviso com link").attachments(java.util.List.of(attachment)).build();

        var result = service.execute(cmd);

        assertThat(result.getAttachments()).hasSize(1);
        assertThat(result.getAttachments().get(0).getExternalUrl()).isEqualTo("https://example.com");
    }

    @Test
    void post_throws_whenContentMissing() {
        when(classroomQueryPort.isMember(PROF_ID, CLASSROOM_ID, ORG_ID, "PROFESSOR")).thenReturn(true);

        var cmd = PostAnnouncementCommand.builder()
                .classroomId(CLASSROOM_ID).organizationId(ORG_ID).authorId(PROF_ID)
                .content("   ").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(EmptyContentException.class);
    }

    @Test
    void post_throws_whenProfessorNotMember() {
        when(classroomQueryPort.isMember(PROF_ID, CLASSROOM_ID, ORG_ID, "PROFESSOR")).thenReturn(false);

        var cmd = PostAnnouncementCommand.builder()
                .classroomId(CLASSROOM_ID).organizationId(ORG_ID).authorId(PROF_ID)
                .content("Aviso").build();

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(UnauthorizedAnnouncementOperationException.class);
    }
}
