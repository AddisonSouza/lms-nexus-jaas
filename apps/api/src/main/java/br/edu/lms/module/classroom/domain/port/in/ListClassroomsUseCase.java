package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;

import java.util.List;

public interface ListClassroomsUseCase {
    List<ClassroomResponse> execute(String organizationId, String requesterId, String requesterRole);
}
