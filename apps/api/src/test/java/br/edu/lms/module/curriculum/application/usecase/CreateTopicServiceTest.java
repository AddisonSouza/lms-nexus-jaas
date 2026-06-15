package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.CreateTopicCommand;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.Subject;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTopicServiceTest {

    @Mock TopicRepository topicRepository;
    @Mock SubjectRepository subjectRepository;

    @InjectMocks CreateTopicService sut;

    @Test
    void shouldCreateTopicWithNextPosition() {
        var subject = Subject.builder().id(SubjectId.of("sub-1")).organizationId("org-1").name("Math").build();
        when(subjectRepository.findById(any(), any())).thenReturn(Optional.of(subject));
        when(topicRepository.maxPositionBySubjectId("sub-1", "org-1")).thenReturn(2);
        when(topicRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(CreateTopicCommand.builder()
                .subjectId("sub-1").organizationId("org-1").title("Unidade 1").build());

        assertThat(result.getTitle()).isEqualTo("Unidade 1");
        assertThat(result.getPosition()).isEqualTo(3);
        assertThat(result.getId()).isNotBlank();
        verify(topicRepository).save(any());
    }

    @Test
    void shouldThrowWhenSubjectNotFound() {
        when(subjectRepository.findById(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(CreateTopicCommand.builder()
                .subjectId("unknown").organizationId("org-1").title("T").build()))
                .isInstanceOf(SubjectNotFoundException.class);
    }

    @Test
    void shouldStartAtPositionOneWhenNoTopicsExist() {
        var subject = Subject.builder().id(SubjectId.of("sub-1")).organizationId("org-1").name("Math").build();
        when(subjectRepository.findById(any(), any())).thenReturn(Optional.of(subject));
        when(topicRepository.maxPositionBySubjectId("sub-1", "org-1")).thenReturn(0);
        when(topicRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(CreateTopicCommand.builder()
                .subjectId("sub-1").organizationId("org-1").title("Intro").build());

        assertThat(result.getPosition()).isEqualTo(1);
    }
}
