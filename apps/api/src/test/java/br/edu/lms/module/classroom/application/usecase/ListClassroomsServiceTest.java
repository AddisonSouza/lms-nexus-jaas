package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.domain.model.Classroom;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import br.edu.lms.module.classroom.domain.model.InviteCode;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListClassroomsServiceTest {

    @Mock ClassroomRepository classroomRepository;

    @InjectMocks ListClassroomsService sut;

    private Classroom classroom(String name) {
        return Classroom.builder()
                .id(ClassroomId.generate())
                .name(name)
                .academicPeriod("2025/1")
                .status(ClassroomStatus.ACTIVE)
                .inviteCode(InviteCode.of("ABC123"))
                .organizationId("org-1")
                .build();
    }

    @Test
    void adminOrgShouldSeeAllClassrooms() {
        when(classroomRepository.findAllByOrganization("org-1"))
                .thenReturn(List.of(classroom("A"), classroom("B")));

        var result = sut.execute("org-1", "user-1", "ADMIN_ORG");

        assertThat(result).hasSize(2);
        verify(classroomRepository).findAllByOrganization("org-1");
        verify(classroomRepository, never()).findAllByMember(any(), any());
    }

    @Test
    void gestorShouldSeeAllClassrooms() {
        when(classroomRepository.findAllByOrganization("org-1")).thenReturn(List.of(classroom("C")));

        var result = sut.execute("org-1", "user-2", "GESTOR");

        assertThat(result).hasSize(1);
    }

    @Test
    void professorShouldSeeOnlyMemberClassrooms() {
        when(classroomRepository.findAllByMember("user-3", "org-1"))
                .thenReturn(List.of(classroom("D")));

        var result = sut.execute("org-1", "user-3", "PROFESSOR");

        assertThat(result).hasSize(1);
        verify(classroomRepository).findAllByMember("user-3", "org-1");
        verify(classroomRepository, never()).findAllByOrganization(any());
    }

    @Test
    void alunoShouldSeeOnlyMemberClassrooms() {
        when(classroomRepository.findAllByMember("user-4", "org-1")).thenReturn(List.of());

        var result = sut.execute("org-1", "user-4", "ALUNO");

        assertThat(result).isEmpty();
    }

    @Test
    void adminOrgGestorAndProfessorShouldSeeInviteCode() {
        when(classroomRepository.findAllByOrganization("org-1")).thenReturn(List.of(classroom("A")));
        when(classroomRepository.findAllByMember("user-3", "org-1")).thenReturn(List.of(classroom("D")));

        assertThat(sut.execute("org-1", "user-1", "ADMIN_ORG").getFirst().getInviteCode()).isEqualTo("ABC123");
        assertThat(sut.execute("org-1", "user-2", "GESTOR").getFirst().getInviteCode()).isEqualTo("ABC123");
        assertThat(sut.execute("org-1", "user-3", "PROFESSOR").getFirst().getInviteCode()).isEqualTo("ABC123");
    }

    @Test
    void alunoShouldNotSeeInviteCode() {
        when(classroomRepository.findAllByMember("user-4", "org-1")).thenReturn(List.of(classroom("E")));

        var result = sut.execute("org-1", "user-4", "ALUNO");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getInviteCode()).isNull();
    }
}
