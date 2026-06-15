package br.edu.lms.module.curriculum.domain.port.out;

public interface OrganizationMemberQueryPort {
    boolean existsByIdAndOrganizationId(String memberId, String organizationId);
    boolean hasProfessorRole(String memberId, String organizationId);
}
