package br.edu.lms.module.curriculum.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class SubjectClassroomId implements Serializable {

    @Column(name = "subject_id", length = 36)
    private String subjectId;

    @Column(name = "classroom_id", length = 36)
    private String classroomId;
}
