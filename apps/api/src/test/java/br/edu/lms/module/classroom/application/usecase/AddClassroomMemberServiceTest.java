package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.AddClassroomMemberCommand;
import br.edu.lms.module.classroom.domain.exception.ClassroomArchivedException;
import br.edu.lms.module.classroom.domain.exception.ClassroomNotFoundException;
import br.edu.lms.module.classroom.domain.exception.MemberNotInOrganizationException;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddClassroomMemberServiceTest {

    @Mock ClassroomRepository classroomRepository;

    @InjectMocks AddClassroomMemberService sut;

    private final ClassroomId classroomId = ClassroomId.generate();
    private final String orgId = "org-1";
    private final String userId = "user-1";

    private Classroom activeClassroom() {
        return Classroom.builder()
                .id(classroomId)
                .name("Turma")
                .academicPeriod("2025/1")
                .status(ClassroomStatus.ACTIVE)
                .inviteCode(InviteCode.of("XYZ123"))
                .organizationId(orgId)
                .build();
    }

    private AddClassroomMemberCommand cmd(ClassroomMemberRole role) {
        return AddClassroomMemberCommand.builder()
                .classroomId(classroomId)
                .userId(userId)
                .organizationId(orgId)
                .role(role)
                .build();
    }

    @Test
    void shouldAddMemberSuccessfully() {
        when(classroomRepository.findById(classroomId, orgId)).thenReturn(Optional.of(activeClassroom()));
        when(classroomRepository.isUserInOrganization(userId, orgId)).thenReturn(true);
        when(classroomRepository.findMember(classroomId, userId)).thenReturn(Optional.empty());
        var saved = ClassroomMember.builder()
                .id(UUID.randomUUID().toString())
                .classroomId(classroomId).userId(userId).organizationId(orgId)
                .role(ClassroomMemberRole.ALUNO).build();
        when(classroomRepository.saveMember(any())).thenReturn(saved);

        var result = sut.execute(cmd(ClassroomMemberRole.ALUNO));

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getRole()).isEqualTo(ClassroomMemberRole.ALUNO);
    }

    @Test
    void shouldBeIdempotentWhenMemberAlreadyExists() {
        var existing = ClassroomMember.builder()
                .id(UUID.randomUUID().toString())
                .classroomId(classroomId).userId(userId).organizationId(orgId)
                .role(ClassroomMemberRole.PROFESSOR).build();
        when(classroomRepository.findById(classroomId, orgId)).thenReturn(Optional.of(activeClassroom()));
        when(classroomRepository.isUserInOrganization(userId, orgId)).thenReturn(true);
        when(classroomRepository.findMember(classroomId, userId)).thenReturn(Optional.of(existing));

        sut.execute(cmd(ClassroomMemberRole.PROFESSOR));

        verify(classroomRepository, never()).saveMember(any());
    }

    @Test
    void shouldThrowWhenUserNotInOrganization() {
        when(classroomRepository.findById(classroomId, orgId)).thenReturn(Optional.of(activeClassroom()));
        when(classroomRepository.isUserInOrganization(userId, orgId)).thenReturn(false);

        assertThatThrownBy(() -> sut.execute(cmd(ClassroomMemberRole.ALUNO)))
                .isInstanceOf(MemberNotInOrganizationException.class);
    }

    @Test
    void shouldThrowWhenClassroomIsArchived() {
        var archived = activeClassroom().toBuilder().status(ClassroomStatus.ARCHIVED).build();
        when(classroomRepository.findById(classroomId, orgId)).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> sut.execute(cmd(ClassroomMemberRole.ALUNO)))
                .isInstanceOf(ClassroomArchivedException.class);
    }

    @Test
    void shouldThrowWhenClassroomNotFound() {
        when(classroomRepository.findById(classroomId, orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(cmd(ClassroomMemberRole.ALUNO)))
                .isInstanceOf(ClassroomNotFoundException.class);
    }
}
