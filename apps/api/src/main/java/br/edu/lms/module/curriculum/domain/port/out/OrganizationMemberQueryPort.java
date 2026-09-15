package br.edu.lms.module.curriculum.domain.port.out;

public interface OrganizationMemberQueryPort {
    boolean existsByIdAndOrganizationId(String memberId, String organizationId);
    // PROFESSOR, GESTOR e ADMIN_ORG podem lecionar (hierarquia ADMIN_ORG > GESTOR > PROFESSOR)
    boolean canTeach(String memberId, String organizationId);
}
