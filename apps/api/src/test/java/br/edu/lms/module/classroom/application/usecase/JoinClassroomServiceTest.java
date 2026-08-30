package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.JoinClassroomCommand;
import br.edu.lms.module.classroom.domain.exception.ClassroomArchivedException;
import br.edu.lms.module.classroom.domain.exception.InvalidInviteCodeException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JoinClassroomServiceTest {

    @Mock ClassroomRepository classroomRepository;

    @InjectMocks JoinClassroomService sut;

    private static final String CODE = "ABC123";
    private static final String USER_ID = "user-1";
    private static final String ORG_ID = "org-1";
    private final ClassroomId classroomId = ClassroomId.generate();

    private Classroom activeClassroom() {
        return Classroom.builder()
                .id(classroomId)
                .name("Turma A")
                .academicPeriod("2025/1")
                .status(ClassroomStatus.ACTIVE)
                .inviteCode(InviteCode.of(CODE))
                .organizationId(ORG_ID)
                .build();
    }

    private JoinClassroomCommand cmd() {
        return JoinClassroomCommand.builder()
                .inviteCode(CODE)
                .userId(USER_ID)
                .organizationId(ORG_ID)
                .build();
    }

    @Test
    void shouldJoinSuccessfully() {
        when(classroomRepository.findByInviteCode(CODE, ORG_ID)).thenReturn(Optional.of(activeClassroom()));
        when(classroomRepository.findMember(classroomId, USER_ID)).thenReturn(Optional.empty());
        var saved = ClassroomMember.builder()
                .id(UUID.randomUUID().toString())
                .classroomId(classroomId).userId(USER_ID).organizationId(ORG_ID)
                .role(ClassroomMemberRole.ALUNO).build();
        when(classroomRepository.saveMember(any())).thenReturn(saved);

        var result = sut.execute(cmd());

        assertThat(result.alreadyMember()).isFalse();
        assertThat(result.classroom().getId()).isEqualTo(classroomId.getValue());
        // Quem entra pelo código entra como ALUNO e nunca recebe o código (RF-08).
        assertThat(result.classroom().getInviteCode()).isNull();
        verify(classroomRepository).saveMember(any());
    }

    @Test
    void shouldBeIdempotentWhenAlreadyMember() {
        var existing = ClassroomMember.builder()
                .id(UUID.randomUUID().toString())
                .classroomId(classroomId).userId(USER_ID).organizationId(ORG_ID)
                .role(ClassroomMemberRole.ALUNO).build();
        when(classroomRepository.findByInviteCode(CODE, ORG_ID)).thenReturn(Optional.of(activeClassroom()));
        when(classroomRepository.findMember(classroomId, USER_ID)).thenReturn(Optional.of(existing));

        var result = sut.execute(cmd());

        assertThat(result.alreadyMember()).isTrue();
        assertThat(result.classroom().getId()).isEqualTo(classroomId.getValue());
        assertThat(result.classroom().getInviteCode()).isNull();
        verify(classroomRepository, never()).saveMember(any());
    }

    @Test
    void shouldThrowWhenInviteCodeInvalid() {
        when(classroomRepository.findByInviteCode(CODE, ORG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(cmd()))
                .isInstanceOf(InvalidInviteCodeException.class);
    }

    @Test
    void shouldThrowWhenClassroomArchived() {
        var archived = activeClassroom().toBuilder().status(ClassroomStatus.ARCHIVED).build();
        when(classroomRepository.findByInviteCode(CODE, ORG_ID)).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> sut.execute(cmd()))
                .isInstanceOf(ClassroomArchivedException.class);
    }
}
