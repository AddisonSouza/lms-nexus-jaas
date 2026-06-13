package br.edu.lms.module.classroom.interfaces.rest.dto;

import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import jakarta.validation.constraints.Size;

public record UpdateClassroomRequest(
        @Size(max = 255) String name,
        @Size(max = 2000) String description,
        @Size(max = 100) String academicPeriod,
        ClassroomStatus status
) {}
