package br.edu.lms.module.classroom.domain.port.out;

import br.edu.lms.module.classroom.domain.model.Classroom;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomMember;

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
    List<ClassroomMember> findMembersByClassroom(ClassroomId classroomId, String organizationId);
    boolean isUserInOrganization(String userId, String organizationId);
    /**
     * O código só resolve dentro da organização de quem procura: um código válido
     * em outra organização é indistinguível de um código inexistente.
     */
    Optional<Classroom> findByInviteCode(String code, String organizationId);
}
