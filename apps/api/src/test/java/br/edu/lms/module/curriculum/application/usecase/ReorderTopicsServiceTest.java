package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.ReorderTopicsCommand;
import br.edu.lms.module.curriculum.domain.model.Topic;
import br.edu.lms.module.curriculum.domain.model.TopicId;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReorderTopicsServiceTest {

    @Mock TopicRepository topicRepository;

    @InjectMocks ReorderTopicsService sut;

    @Test
    void shouldUpdatePositionsInGivenOrder() {
        var t1 = Topic.builder().id(TopicId.of("t-1")).subjectId("s-1").organizationId("org-1").title("A").position(1).build();
        var t2 = Topic.builder().id(TopicId.of("t-2")).subjectId("s-1").organizationId("org-1").title("B").position(2).build();
        when(topicRepository.findBySubjectId("s-1", "org-1")).thenReturn(List.of(t1, t2));
        when(topicRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(ReorderTopicsCommand.builder()
                .subjectId("s-1").organizationId("org-1").topicIds(List.of("t-2", "t-1")).build());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("t-2");
        assertThat(result.get(0).getPosition()).isEqualTo(1);
        assertThat(result.get(1).getPosition()).isEqualTo(2);
    }

    @Test
    void shouldRejectTopicFromDifferentSubject() {
        var t1 = Topic.builder().id(TopicId.of("t-1")).subjectId("s-1").organizationId("org-1").title("A").position(1).build();
        when(topicRepository.findBySubjectId("s-1", "org-1")).thenReturn(List.of(t1));

        assertThatThrownBy(() -> sut.execute(ReorderTopicsCommand.builder()
                .subjectId("s-1").organizationId("org-1").topicIds(List.of("t-1", "alien-id")).build()))
                .isInstanceOf(BadRequestException.class);
    }
}
