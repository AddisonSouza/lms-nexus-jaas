package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.CreateSubjectCommand;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSubjectServiceTest {

    @Mock SubjectRepository subjectRepository;

    @InjectMocks CreateSubjectService sut;

    @Test
    void shouldCreateSubjectWithCorrectOrganizationId() {
        when(subjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(CreateSubjectCommand.builder()
                .name("Matemática")
                .organizationId("org-1")
                .build());

        assertThat(result.getName()).isEqualTo("Matemática");
        assertThat(result.getOrganizationId()).isEqualTo("org-1");
        assertThat(result.getId()).isNotBlank();
    }

    @Test
    void shouldCreateSubjectWithOptionalFields() {
        when(subjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(CreateSubjectCommand.builder()
                .name("Física")
                .code("FIS101")
                .description("Mecânica Clássica")
                .workloadHours(80)
                .organizationId("org-1")
                .build());

        assertThat(result.getCode()).isEqualTo("FIS101");
        assertThat(result.getDescription()).isEqualTo("Mecânica Clássica");
        assertThat(result.getWorkloadHours()).isEqualTo(80);
    }

    @Test
    void shouldCreateSubjectWithoutOptionalFields() {
        when(subjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(CreateSubjectCommand.builder()
                .name("Química")
                .organizationId("org-1")
                .build());

        assertThat(result.getCode()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getWorkloadHours()).isNull();
        assertThat(result.getClassroomIds()).isEmpty();
        assertThat(result.getTeacherMemberIds()).isEmpty();
    }

    @Test
    void shouldPersistSubjectViaSave() {
        when(subjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.execute(CreateSubjectCommand.builder().name("Bio").organizationId("org-1").build());

        verify(subjectRepository, times(1)).save(any());
    }
}
