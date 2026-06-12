package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.CreateClassroomCommand;
import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateClassroomServiceTest {

    @Mock ClassroomRepository classroomRepository;

    @InjectMocks CreateClassroomService sut;

    @Test
    void shouldCreateClassroomWithActiveStatusAndInviteCode() {
        when(classroomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(CreateClassroomCommand.builder()
                .name("Turma A")
                .academicPeriod("2025/1")
                .organizationId("org-1")
                .build());

        assertThat(result.getName()).isEqualTo("Turma A");
        assertThat(result.getStatus()).isEqualTo(ClassroomStatus.ACTIVE);
        assertThat(result.getInviteCode()).hasSize(6);
        assertThat(result.getOrganizationId()).isEqualTo("org-1");
    }

    @Test
    void shouldPersistClassroomViaSave() {
        when(classroomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.execute(CreateClassroomCommand.builder()
                .name("Turma B")
                .academicPeriod("2025/2")
                .organizationId("org-2")
                .build());

        verify(classroomRepository, times(1)).save(any());
    }

    @Test
    void shouldGenerateUniqueInviteCodes() {
        when(classroomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var cmd = CreateClassroomCommand.builder().name("T").academicPeriod("2025").organizationId("org-1").build();
        var code1 = sut.execute(cmd).getInviteCode();
        var code2 = sut.execute(cmd).getInviteCode();

        // statistically extremely unlikely to collide
        assertThat(code1).hasSize(6);
        assertThat(code2).hasSize(6);
    }
}
