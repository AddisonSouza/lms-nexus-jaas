package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.LinkClassroomCommand;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.Subject;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkSubjectToClassroomServiceTest {

    @Mock SubjectRepository subjectRepository;
    @Mock ClassroomQueryPort classroomQueryPort;

    @InjectMocks LinkSubjectToClassroomService sut;

    private Subject stubSubject(String id) {
        return Subject.builder()
                .id(SubjectId.of(id))
                .name("Test")
                .organizationId("org-1")
                .build();
    }

    @Test
    void shouldLinkClassroomSuccessfully() {
        var subjectId = SubjectId.of("sub-1");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.of(stubSubject("sub-1")));
        when(classroomQueryPort.existsByIdAndOrganizationId("cls-1", "org-1")).thenReturn(true);
        when(classroomQueryPort.isArchived("cls-1")).thenReturn(false);
        when(subjectRepository.existsSubjectClassroomLink("sub-1", "cls-1")).thenReturn(false);

        sut.execute(subjectId, LinkClassroomCommand.builder().classroomId("cls-1").organizationId("org-1").build());

        verify(subjectRepository).saveSubjectClassroomLink("sub-1", "cls-1");
    }

    @Test
    void shouldBeIdempotentWhenLinkAlreadyExists() {
        var subjectId = SubjectId.of("sub-1");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.of(stubSubject("sub-1")));
        when(classroomQueryPort.existsByIdAndOrganizationId("cls-1", "org-1")).thenReturn(true);
        when(classroomQueryPort.isArchived("cls-1")).thenReturn(false);
        when(subjectRepository.existsSubjectClassroomLink("sub-1", "cls-1")).thenReturn(true);

        sut.execute(subjectId, LinkClassroomCommand.builder().classroomId("cls-1").organizationId("org-1").build());

        verify(subjectRepository, never()).saveSubjectClassroomLink(any(), any());
    }

    @Test
    void shouldThrowWhenSubjectNotFound() {
        var subjectId = SubjectId.of("sub-x");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(subjectId,
                LinkClassroomCommand.builder().classroomId("cls-1").organizationId("org-1").build()))
                .isInstanceOf(SubjectNotFoundException.class);
    }

    @Test
    void shouldThrowWhenClassroomNotInOrganization() {
        var subjectId = SubjectId.of("sub-1");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.of(stubSubject("sub-1")));
        when(classroomQueryPort.existsByIdAndOrganizationId("cls-x", "org-1")).thenReturn(false);

        assertThatThrownBy(() -> sut.execute(subjectId,
                LinkClassroomCommand.builder().classroomId("cls-x").organizationId("org-1").build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenClassroomIsArchived() {
        var subjectId = SubjectId.of("sub-1");
        when(subjectRepository.findById(subjectId, "org-1")).thenReturn(Optional.of(stubSubject("sub-1")));
        when(classroomQueryPort.existsByIdAndOrganizationId("cls-1", "org-1")).thenReturn(true);
        when(classroomQueryPort.isArchived("cls-1")).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(subjectId,
                LinkClassroomCommand.builder().classroomId("cls-1").organizationId("org-1").build()))
                .isInstanceOf(WebApplicationException.class);
    }
}
