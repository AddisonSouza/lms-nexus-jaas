package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.exception.ContentNotFoundException;
import br.edu.lms.module.curriculum.domain.model.ContentType;
import br.edu.lms.module.curriculum.domain.model.SubjectContent;
import br.edu.lms.module.curriculum.domain.model.SubjectContentId;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteContentServiceTest {

    @Mock ContentRepository contentRepository;
    @Mock StoragePort storagePort;

    @InjectMocks DeleteContentService sut;

    @Test
    void shouldSoftDeleteContentAndRemoveFile() {
        var content = SubjectContent.builder()
                .id(SubjectContentId.of("c-1")).topicId("t-1").organizationId("org-1")
                .title("PDF").contentType(ContentType.DOCUMENTO).fileKey("lesson/file.pdf").position(1).build();
        when(contentRepository.findById("c-1", "org-1")).thenReturn(Optional.of(content));
        when(contentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.execute("c-1", "sub-1", "org-1");

        verify(storagePort).delete(eq("lesson/file.pdf"));
        verify(contentRepository).save(argThat(c -> c.getDeletedAt() != null));
    }

    @Test
    void shouldSoftDeleteWithoutStorageCallWhenNoFileKey() {
        var content = SubjectContent.builder()
                .id(SubjectContentId.of("c-2")).topicId("t-1").organizationId("org-1")
                .title("Link").contentType(ContentType.LINK).externalUrl("https://x.com").position(1).build();
        when(contentRepository.findById("c-2", "org-1")).thenReturn(Optional.of(content));
        when(contentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.execute("c-2", "sub-1", "org-1");

        verifyNoInteractions(storagePort);
    }

    @Test
    void shouldThrowWhenContentNotFound() {
        when(contentRepository.findById("bad", "org-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute("bad", "sub-1", "org-1"))
                .isInstanceOf(ContentNotFoundException.class);
    }
}
