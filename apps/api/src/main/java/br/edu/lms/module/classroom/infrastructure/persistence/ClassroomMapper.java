package br.edu.lms.module.classroom.infrastructure.persistence;

import br.edu.lms.module.classroom.domain.model.Classroom;
import br.edu.lms.module.classroom.domain.model.ClassroomMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface ClassroomMapper {

    @Mapping(target = "id", expression = "java(br.edu.lms.module.classroom.domain.model.ClassroomId.of(entity.getId()))")
    @Mapping(target = "status", expression = "java(br.edu.lms.module.classroom.domain.model.ClassroomStatus.valueOf(entity.getStatus()))")
    @Mapping(target = "inviteCode", expression = "java(br.edu.lms.module.classroom.domain.model.InviteCode.of(entity.getInviteCode()))")
    Classroom toDomain(ClassroomJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "inviteCode", expression = "java(domain.getInviteCode().getValue())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ClassroomJpaEntity toEntity(Classroom domain);

    @Mapping(target = "classroomId", expression = "java(br.edu.lms.module.classroom.domain.model.ClassroomId.of(entity.getClassroomId()))")
    @Mapping(target = "role", expression = "java(br.edu.lms.module.classroom.domain.model.ClassroomMemberRole.valueOf(entity.getRole()))")
    ClassroomMember toMemberDomain(ClassroomMemberJpaEntity entity);

    @Mapping(target = "classroomId", expression = "java(domain.getClassroomId().getValue())")
    @Mapping(target = "role", expression = "java(domain.getRole().name())")
    @Mapping(target = "joinedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ClassroomMemberJpaEntity toMemberEntity(ClassroomMember domain);
}
