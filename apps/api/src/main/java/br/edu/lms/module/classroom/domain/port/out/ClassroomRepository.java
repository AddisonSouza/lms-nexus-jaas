package br.edu.lms.module.classroom.domain.port.out;

import br.edu.lms.module.classroom.domain.model.Classroom;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomMember;
import br.edu.lms.module.classroom.domain.model.ClassroomMemberRole;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository {
    Classroom save(Classroom classroom);
    Optional<Classroom> findById(ClassroomId id, String organizationId);
    List<Classroom> findAllByOrganization(String organizationId);
    List<Classroom> findAllByMember(String userId, String organizationId);
    void softDelete(ClassroomId id, String organizationId);

    Optional<ClassroomMember> findMember(ClassroomId classroomId, String userId);
    ClassroomMember saveMember(ClassroomMember member);
    void softDeleteMember(ClassroomId classroomId, String userId);

    /** O vínculo removido (soft delete) deste usuário na turma, se houver. */
    Optional<ClassroomMember> findRemovedMember(ClassroomId classroomId, String userId);

    /** Traz de volta um vínculo removido: ativo de novo, com o papel dado e ingresso agora. */
    void reactivateMember(String memberId, ClassroomMemberRole role);
    List<ClassroomMember> findMembersByClassroom(ClassroomId classroomId, String organizationId);
    boolean isUserInOrganization(String userId, String organizationId);
    /**
     * O código só resolve dentro da organização de quem procura: um código válido
     * em outra organização é indistinguível de um código inexistente.
     */
    Optional<Classroom> findByInviteCode(String code, String organizationId);
}
