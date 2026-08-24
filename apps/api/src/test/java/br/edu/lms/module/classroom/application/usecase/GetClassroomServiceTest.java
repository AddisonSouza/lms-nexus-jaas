package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.domain.exception.ClassroomNotFoundException;
import br.edu.lms.module.classroom.domain.model.Classroom;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomMember;
import br.edu.lms.module.classroom.domain.model.ClassroomMemberRole;
import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import br.edu.lms.module.classroom.domain.model.InviteCode;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetClassroomServiceTest {

    @Mock ClassroomRepository classroomRepository;

    @InjectMocks GetClassroomService sut;

    private final ClassroomId classroomId = ClassroomId.generate();

    private Classroom classroom() {
        return Classroom.builder()
                .id(classroomId)
                .name("Turma A")
                .academicPeriod("2025/1")
                .status(ClassroomStatus.ACTIVE)
                .inviteCode(InviteCode.of("ABC123"))
                .organizationId("org-1")
                .build();
    }

    private ClassroomMember member(String userId, ClassroomMemberRole role) {
        return ClassroomMember.builder()
                .id("member-1")
                .classroomId(classroomId)
                .userId(userId)
                .organizationId("org-1")
                .role(role)
                .build();
    }

    @Test
    void adminOrgShouldSeeInviteCode() {
        when(classroomRepository.findById(classroomId, "org-1")).thenReturn(Optional.of(classroom()));

        var result = sut.execute(classroomId, "user-1", "org-1", "ADMIN_ORG");

        assertThat(result.getInviteCode()).isEqualTo("ABC123");
        verify(classroomRepository, never()).findMember(any(), any());
    }

    @Test
    void gestorShouldSeeInviteCode() {
        when(classroomRepository.findById(classroomId, "org-1")).thenReturn(Optional.of(classroom()));

        var result = sut.execute(classroomId, "user-2", "org-1", "GESTOR");

        assertThat(result.getInviteCode()).isEqualTo("ABC123");
    }

    @Test
    void professorMemberShouldSeeInviteCode() {
        when(classroomRepository.findById(classroomId, "org-1")).thenReturn(Optional.of(classroom()));
        when(classroomRepository.findMember(classroomId, "user-3"))
                .thenReturn(Optional.of(member("user-3", ClassroomMemberRole.PROFESSOR)));

        var result = sut.execute(classroomId, "user-3", "org-1", "PROFESSOR");

        assertThat(result.getInviteCode()).isEqualTo("ABC123");
    }

    @Test
    void alunoMemberShouldNotSeeInviteCode() {
        when(classroomRepository.findById(classroomId, "org-1")).thenReturn(Optional.of(classroom()));
        when(classroomRepository.findMember(classroomId, "user-4"))
                .thenReturn(Optional.of(member("user-4", ClassroomMemberRole.ALUNO)));

        var result = sut.execute(classroomId, "user-4", "org-1", "ALUNO");

        assertThat(result.getName()).isEqualTo("Turma A");
        assertThat(result.getInviteCode()).isNull();
    }

    @Test
    void nonMemberShouldNotFindClassroom() {
        when(classroomRepository.findById(classroomId, "org-1")).thenReturn(Optional.of(classroom()));
        when(classroomRepository.findMember(classroomId, "user-5")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(classroomId, "user-5", "org-1", "ALUNO"))
                .isInstanceOf(ClassroomNotFoundException.class);
    }
}
