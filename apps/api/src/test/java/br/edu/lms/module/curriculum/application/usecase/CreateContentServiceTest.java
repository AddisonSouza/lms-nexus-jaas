package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.CreateContentCommand;
import br.edu.lms.module.curriculum.domain.exception.InvalidFileTypeException;
import br.edu.lms.module.curriculum.domain.exception.TopicNotFoundException;
import br.edu.lms.module.curriculum.domain.model.ContentType;
import br.edu.lms.module.curriculum.domain.model.Topic;
import br.edu.lms.module.curriculum.domain.model.TopicId;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import br.edu.lms.module.storage.domain.model.StoredFile;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateContentServiceTest {

    @Mock ContentRepository contentRepository;
    @Mock TopicRepository topicRepository;
    @Mock StoragePort storagePort;

    @InjectMocks CreateContentService sut;

    private Topic aTopicOf(String id, String orgId) {
        return Topic.builder().id(TopicId.of(id)).subjectId("sub-1").organizationId(orgId).title("T").position(1).build();
    }

    @Test
    void shouldCreateLinkContentWithoutUpload() {
        when(topicRepository.findById("t-1", "org-1")).thenReturn(Optional.of(aTopicOf("t-1", "org-1")));
        when(contentRepository.maxPositionByTopicId(any(), any())).thenReturn(0);
        when(contentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(CreateContentCommand.builder()
                .topicId("t-1").subjectId("sub-1").organizationId("org-1")
                .title("Link externo").contentType(ContentType.LINK)
                .externalUrl("https://example.com").build());

        assertThat(result.getContentType()).isEqualTo(ContentType.LINK);
        assertThat(result.getExternalUrl()).isEqualTo("https://example.com");
        assertThat(result.getFileKey()).isNull();
        verifyNoInteractions(storagePort);
    }

    @Test
    void shouldUploadFileAndStoreKey() {
        when(topicRepository.findById("t-1", "org-1")).thenReturn(Optional.of(aTopicOf("t-1", "org-1")));
        when(contentRepository.maxPositionByTopicId(any(), any())).thenReturn(0);
        when(contentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(storagePort.store(any(), any(), any(), anyLong(), any(StorageContext.class)))
                .thenReturn(new StoredFile("lesson_material/2025/01/uuid-file.pdf", "file.pdf", "application/pdf", 1024));

        var result = sut.execute(CreateContentCommand.builder()
                .topicId("t-1").subjectId("sub-1").organizationId("org-1")
                .title("PDF").contentType(ContentType.DOCUMENTO)
                .fileStream(new ByteArrayInputStream(new byte[]{1}))
                .fileName("file.pdf").fileMimeType("application/pdf").fileSizeBytes(1024).build());

        assertThat(result.getFileKey()).isEqualTo("lesson_material/2025/01/uuid-file.pdf");
    }

    @Test
    void shouldRejectDisallowedMimeType() {
        when(topicRepository.findById("t-1", "org-1")).thenReturn(Optional.of(aTopicOf("t-1", "org-1")));

        assertThatThrownBy(() -> sut.execute(CreateContentCommand.builder()
                .topicId("t-1").subjectId("sub-1").organizationId("org-1")
                .title("Exec").contentType(ContentType.ARQUIVO)
                .fileStream(new ByteArrayInputStream(new byte[]{1}))
                .fileName("virus.exe").fileMimeType("application/x-msdownload").fileSizeBytes(100).build()))
                .isInstanceOf(InvalidFileTypeException.class);
    }

    @Test
    void shouldThrowWhenTopicNotFound() {
        when(topicRepository.findById("bad", "org-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(CreateContentCommand.builder()
                .topicId("bad").subjectId("sub-1").organizationId("org-1")
                .title("T").contentType(ContentType.LINK).externalUrl("https://x.com").build()))
                .isInstanceOf(TopicNotFoundException.class);
    }
}
