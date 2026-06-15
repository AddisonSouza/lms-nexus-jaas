package br.edu.lms.module.curriculum.domain;

import br.edu.lms.module.curriculum.domain.model.Subject;
import br.edu.lms.module.curriculum.domain.model.SubjectCode;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class SubjectTest {

    @Test
    void shouldBuildSubjectWithAllFields() {
        var subject = Subject.builder()
                .id(SubjectId.generate())
                .name("Matemática")
                .code(SubjectCode.of("MAT101"))
                .description("Álgebra Linear")
                .workloadHours(60)
                .organizationId("org-1")
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(subject.getName()).isEqualTo("Matemática");
        assertThat(subject.getCode().getValue()).isEqualTo("MAT101");
        assertThat(subject.getWorkloadHours()).isEqualTo(60);
        assertThat(subject.getOrganizationId()).isEqualTo("org-1");
        assertThat(subject.getDeletedAt()).isNull();
    }

    @Test
    void shouldBuildSubjectWithoutOptionalFields() {
        var subject = Subject.builder()
                .id(SubjectId.generate())
                .name("Física")
                .organizationId("org-1")
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(subject.getName()).isEqualTo("Física");
        assertThat(subject.getCode()).isNull();
        assertThat(subject.getDescription()).isNull();
        assertThat(subject.getWorkloadHours()).isNull();
    }

    @Test
    void shouldNormalizeSubjectCode() {
        var code = SubjectCode.of("mat101");
        assertThat(code.getValue()).isEqualTo("MAT101");
    }

    @Test
    void shouldRejectCodeExceeding20Chars() {
        assertThatThrownBy(() -> new SubjectCode("TOOLONGCODETHATEXCEEDS20CHARACTERS"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnNullForBlankCode() {
        assertThat(SubjectCode.of("")).isNull();
        assertThat(SubjectCode.of(null)).isNull();
    }

    @Test
    void shouldGenerateUniqueIds() {
        var id1 = SubjectId.generate();
        var id2 = SubjectId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldSupportToBuilderForUpdate() {
        var original = Subject.builder()
                .id(SubjectId.generate())
                .name("Original")
                .organizationId("org-1")
                .createdAt(LocalDateTime.now())
                .build();

        var updated = original.toBuilder().name("Updated").build();

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getId()).isEqualTo(original.getId());
    }
}
