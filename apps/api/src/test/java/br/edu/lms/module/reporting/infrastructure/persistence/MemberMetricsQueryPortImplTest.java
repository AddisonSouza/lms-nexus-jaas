package br.edu.lms.module.reporting.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberMetricsQueryPortImplTest {

    @Test
    void memberJoinedDescription_usesNameAndReadableRole() {
        assertThat(MemberMetricsQueryPortImpl.memberJoinedDescription("Maria Silva", "PROFESSOR"))
                .isEqualTo("Maria Silva (Professor) ingressou na organização");
    }

    @Test
    void memberJoinedDescription_withoutName_fallsBackToNewMember() {
        assertThat(MemberMetricsQueryPortImpl.memberJoinedDescription(null, "GESTOR"))
                .isEqualTo("Novo membro (Gestor) ingressou na organização");
        assertThat(MemberMetricsQueryPortImpl.memberJoinedDescription("  ", "ADMIN_ORG"))
                .isEqualTo("Novo membro (Administrador) ingressou na organização");
    }

    @Test
    void memberJoinedDescription_unknownRole_keepsRawValue() {
        assertThat(MemberMetricsQueryPortImpl.memberJoinedDescription("Maria Silva", "OUTRO"))
                .isEqualTo("Maria Silva (OUTRO) ingressou na organização");
    }
}
