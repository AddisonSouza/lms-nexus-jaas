package br.edu.lms.module.curriculum.domain.port.out;

import java.util.List;

public interface OrganizationMemberQueryPort {
    boolean existsByIdAndOrganizationId(String memberId, String organizationId);
    // PROFESSOR, GESTOR e ADMIN_ORG podem lecionar (hierarquia ADMIN_ORG > GESTOR > PROFESSOR)
    boolean canTeach(String memberId, String organizationId);

    /**
     * Usuários por trás de um conjunto de membros. O vínculo de professor guarda
     * o `memberId`, mas o JWT só traz o `userId` — sem esta tradução o frontend
     * não tem como saber se quem está olhando leciona a disciplina.
     */
    List<String> findUserIdsByMemberIds(List<String> memberIds, String organizationId);
}
