package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.exception.TopicNotFoundException;
import br.edu.lms.module.curriculum.domain.model.Topic;
import br.edu.lms.module.curriculum.domain.model.TopicId;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
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
class DeleteTopicServiceTest {

    @Mock TopicRepository topicRepository;
    @Mock ContentRepository contentRepository;

    @InjectMocks DeleteTopicService sut;

    @Test
    void shouldSoftDeleteTopicAndCascadeContents() {
        var topic = Topic.builder().id(TopicId.of("t-1")).subjectId("sub-1").organizationId("org-1").title("T1").position(1).build();
        when(topicRepository.findById("t-1", "org-1")).thenReturn(Optional.of(topic));
        when(topicRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.execute("t-1", "sub-1", "org-1");

        verify(contentRepository).softDeleteByTopicId(eq("t-1"), eq("org-1"));
        verify(topicRepository).save(argThat(t -> t.getDeletedAt() != null));
    }

    @Test
    void shouldThrowWhenTopicNotFound() {
        when(topicRepository.findById("missing", "org-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute("missing", "sub-1", "org-1"))
                .isInstanceOf(TopicNotFoundException.class);
    }
}
