package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.domain.exception.ClassroomArchivedException;
import br.edu.lms.module.classroom.domain.exception.ClassroomNotFoundException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegenerateInviteCodeServiceTest {

    @Mock ClassroomRepository classroomRepository;

    @InjectMocks RegenerateInviteCodeService sut;

    private final ClassroomId classroomId = ClassroomId.generate();
    private final String orgId = "org-1";

    private Classroom activeClassroom() {
        return Classroom.builder()
                .id(classroomId)
                .name("Turma A")
                .academicPeriod("2025/1")
                .status(ClassroomStatus.ACTIVE)
                .inviteCode(InviteCode.of("OLD123"))
                .organizationId(orgId)
                .build();
    }

    @Test
    void shouldRegenerateCodeSuccessfully() {
        when(classroomRepository.findById(classroomId, orgId)).thenReturn(Optional.of(activeClassroom()));
        when(classroomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(classroomId, orgId);

        assertThat(result.getInviteCode()).isNotEqualTo("OLD123");
        assertThat(result.getInviteCode()).hasSize(6);
    }

    @Test
    void shouldThrowWhenClassroomNotFound() {
        when(classroomRepository.findById(classroomId, orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(classroomId, orgId))
                .isInstanceOf(ClassroomNotFoundException.class);
    }

    @Test
    void shouldThrowWhenClassroomArchived() {
        var archived = activeClassroom().toBuilder().status(ClassroomStatus.ARCHIVED).build();
        when(classroomRepository.findById(classroomId, orgId)).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> sut.execute(classroomId, orgId))
                .isInstanceOf(ClassroomArchivedException.class);
    }
}
