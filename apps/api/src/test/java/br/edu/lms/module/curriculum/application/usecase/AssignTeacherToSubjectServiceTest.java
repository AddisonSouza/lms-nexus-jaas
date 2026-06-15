package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.AssignTeacherCommand;
import br.edu.lms.module.curriculum.domain.exception.InvalidTeacherAssignmentException;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.Subject;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.out.OrganizationMemberQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignTeacherToSubjectServiceTest {

    @Mock SubjectRepository subjectRepository;
    @Mock OrganizationMemberQueryPort memberQueryPort;

    @InjectMocks AssignTeacherToSubjectService sut;

    private Subject stubSubject(String id) {
        return Subject.builder().id(SubjectId.of(id)).name("Test").organizationId("org-1").build();
    }

    @Test
    void shouldAssignTeacherSuccessfully() {
        var subjectId = SubjectId.of("sub-1");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.of(stubSubject("sub-1")));
        when(memberQueryPort.existsByIdAndOrganizationId("mem-1", "org-1")).thenReturn(true);
        when(memberQueryPort.hasProfessorRole("mem-1", "org-1")).thenReturn(true);
        when(subjectRepository.existsSubjectTeacherLink("sub-1", "mem-1")).thenReturn(false);

        sut.execute(subjectId, AssignTeacherCommand.builder().memberId("mem-1").organizationId("org-1").build());

        verify(subjectRepository).saveSubjectTeacherLink("sub-1", "mem-1");
    }

    @Test
    void shouldBeIdempotentWhenAlreadyAssigned() {
        var subjectId = SubjectId.of("sub-1");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.of(stubSubject("sub-1")));
        when(memberQueryPort.existsByIdAndOrganizationId("mem-1", "org-1")).thenReturn(true);
        when(memberQueryPort.hasProfessorRole("mem-1", "org-1")).thenReturn(true);
        when(subjectRepository.existsSubjectTeacherLink("sub-1", "mem-1")).thenReturn(true);

        sut.execute(subjectId, AssignTeacherCommand.builder().memberId("mem-1").organizationId("org-1").build());

        verify(subjectRepository, never()).saveSubjectTeacherLink(any(), any());
    }

    @Test
    void shouldThrowWhenSubjectNotFound() {
        var subjectId = SubjectId.of("sub-x");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(subjectId,
                AssignTeacherCommand.builder().memberId("mem-1").organizationId("org-1").build()))
                .isInstanceOf(SubjectNotFoundException.class);
    }

    @Test
    void shouldThrowWhenMemberNotInOrganization() {
        var subjectId = SubjectId.of("sub-1");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.of(stubSubject("sub-1")));
        when(memberQueryPort.existsByIdAndOrganizationId("mem-x", "org-1")).thenReturn(false);

        assertThatThrownBy(() -> sut.execute(subjectId,
                AssignTeacherCommand.builder().memberId("mem-x").organizationId("org-1").build()))
                .isInstanceOf(InvalidTeacherAssignmentException.class)
                .hasMessage("MEMBER_NOT_IN_ORGANIZATION");
    }

    @Test
    void shouldThrowWhenMemberIsNotProfessor() {
        var subjectId = SubjectId.of("sub-1");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.of(stubSubject("sub-1")));
        when(memberQueryPort.existsByIdAndOrganizationId("mem-1", "org-1")).thenReturn(true);
        when(memberQueryPort.hasProfessorRole("mem-1", "org-1")).thenReturn(false);

        assertThatThrownBy(() -> sut.execute(subjectId,
                AssignTeacherCommand.builder().memberId("mem-1").organizationId("org-1").build()))
                .isInstanceOf(InvalidTeacherAssignmentException.class)
                .hasMessage("MEMBER_NOT_A_PROFESSOR");
    }

    @Test
    void shouldAllowProfessorAssignedToMultipleSubjects() {
        var sub1 = SubjectId.of("sub-1");
        var sub2 = SubjectId.of("sub-2");
        when(subjectRepository.findById(sub1, "org-1")).thenReturn(Optional.of(stubSubject("sub-1")));
        when(subjectRepository.findById(sub2, "org-1")).thenReturn(Optional.of(stubSubject("sub-2")));
        when(memberQueryPort.existsByIdAndOrganizationId("mem-1", "org-1")).thenReturn(true);
        when(memberQueryPort.hasProfessorRole("mem-1", "org-1")).thenReturn(true);
        when(subjectRepository.existsSubjectTeacherLink(anyString(), eq("mem-1"))).thenReturn(false);

        sut.execute(sub1, AssignTeacherCommand.builder().memberId("mem-1").organizationId("org-1").build());
        sut.execute(sub2, AssignTeacherCommand.builder().memberId("mem-1").organizationId("org-1").build());

        verify(subjectRepository).saveSubjectTeacherLink("sub-1", "mem-1");
        verify(subjectRepository).saveSubjectTeacherLink("sub-2", "mem-1");
    }
}
