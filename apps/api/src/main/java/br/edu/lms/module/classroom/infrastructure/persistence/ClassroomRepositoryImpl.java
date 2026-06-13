package br.edu.lms.module.classroom.infrastructure.persistence;

import br.edu.lms.module.classroom.domain.model.Classroom;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomMember;
import br.edu.lms.module.classroom.domain.model.ClassroomMemberRole;
import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import br.edu.lms.module.classroom.domain.model.InviteCode;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class ClassroomRepositoryImpl implements ClassroomRepository {

    private final EntityManager em;

    @Override
    @Transactional
    public Classroom save(Classroom classroom) {
        var entity = new ClassroomJpaEntity();
        entity.setId(classroom.getId().getValue());
        entity.setOrganizationId(classroom.getOrganizationId());
        entity.setName(classroom.getName());
        entity.setDescription(classroom.getDescription());
        entity.setAcademicPeriod(classroom.getAcademicPeriod());
        entity.setStatus(classroom.getStatus().name());
        entity.setInviteCode(classroom.getInviteCode().getValue());
        em.merge(entity);
        return classroom;
    }

    @Override
    public Optional<Classroom> findById(ClassroomId id, String organizationId) {
        return Optional.ofNullable(em.find(ClassroomJpaEntity.class, id.getValue()))
                .filter(e -> e.getDeletedAt() == null && e.getOrganizationId().equals(organizationId))
                .map(this::toDomain);
    }

    @Override
    public List<Classroom> findAllByOrganization(String organizationId) {
        return em.createQuery(
                        "SELECT c FROM ClassroomJpaEntity c WHERE c.organizationId = :orgId AND c.deletedAt IS NULL",
                        ClassroomJpaEntity.class)
                .setParameter("orgId", organizationId)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Classroom> findAllByMember(String userId, String organizationId) {
        return em.createQuery(
                        "SELECT c FROM ClassroomJpaEntity c " +
                        "WHERE c.organizationId = :orgId AND c.deletedAt IS NULL " +
                        "AND EXISTS (SELECT m FROM ClassroomMemberJpaEntity m " +
                        "           WHERE m.classroomId = c.id AND m.userId = :userId AND m.deletedAt IS NULL)",
                        ClassroomJpaEntity.class)
                .setParameter("orgId", organizationId)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void softDelete(ClassroomId id, String organizationId) {
        em.createQuery(
                        "UPDATE ClassroomJpaEntity c SET c.deletedAt = :now WHERE c.id = :id AND c.organizationId = :orgId")
                .setParameter("now", LocalDateTime.now())
                .setParameter("id", id.getValue())
                .setParameter("orgId", organizationId)
                .executeUpdate();
        em.createQuery(
                        "UPDATE ClassroomMemberJpaEntity m SET m.deletedAt = :now WHERE m.classroomId = :cid AND m.deletedAt IS NULL")
                .setParameter("now", LocalDateTime.now())
                .setParameter("cid", id.getValue())
                .executeUpdate();
    }

    @Override
    public Optional<ClassroomMember> findMember(ClassroomId classroomId, String userId) {
        return em.createQuery(
                        "SELECT m FROM ClassroomMemberJpaEntity m " +
                        "WHERE m.classroomId = :cid AND m.userId = :uid AND m.deletedAt IS NULL",
                        ClassroomMemberJpaEntity.class)
                .setParameter("cid", classroomId.getValue())
                .setParameter("uid", userId)
                .getResultStream()
                .findFirst()
                .map(this::toMemberDomain);
    }

    @Override
    @Transactional
    public ClassroomMember saveMember(ClassroomMember member) {
        var entity = new ClassroomMemberJpaEntity();
        entity.setId(member.getId());
        entity.setClassroomId(member.getClassroomId().getValue());
        entity.setUserId(member.getUserId());
        entity.setOrganizationId(member.getOrganizationId());
        entity.setRole(member.getRole().name());
        em.merge(entity);
        return member;
    }

    @Override
    @Transactional
    public void softDeleteMember(ClassroomId classroomId, String userId) {
        em.createQuery(
                        "UPDATE ClassroomMemberJpaEntity m SET m.deletedAt = :now " +
                        "WHERE m.classroomId = :cid AND m.userId = :uid AND m.deletedAt IS NULL")
                .setParameter("now", LocalDateTime.now())
                .setParameter("cid", classroomId.getValue())
                .setParameter("uid", userId)
                .executeUpdate();
    }

    @Override
    public List<ClassroomMember> findMembersByClassroom(ClassroomId classroomId, String organizationId) {
        return em.createQuery(
                        "SELECT m FROM ClassroomMemberJpaEntity m " +
                        "WHERE m.classroomId = :cid AND m.organizationId = :orgId AND m.deletedAt IS NULL",
                        ClassroomMemberJpaEntity.class)
                .setParameter("cid", classroomId.getValue())
                .setParameter("orgId", organizationId)
                .getResultList()
                .stream()
                .map(this::toMemberDomain)
                .toList();
    }

    @Override
    public boolean isUserInOrganization(String userId, String organizationId) {
        var count = em.createQuery(
                        "SELECT COUNT(m) FROM br.edu.lms.module.organization.infrastructure.persistence.OrganizationMemberJpaEntity m " +
                        "WHERE m.userId = :userId AND m.organizationId = :orgId AND m.deletedAt IS NULL",
                        Long.class)
                .setParameter("userId", userId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
        return count > 0;
    }

    private Classroom toDomain(ClassroomJpaEntity e) {
        return Classroom.builder()
                .id(ClassroomId.of(e.getId()))
                .organizationId(e.getOrganizationId())
                .name(e.getName())
                .description(e.getDescription())
                .academicPeriod(e.getAcademicPeriod())
                .status(ClassroomStatus.valueOf(e.getStatus()))
                .inviteCode(InviteCode.of(e.getInviteCode()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }

    private ClassroomMember toMemberDomain(ClassroomMemberJpaEntity e) {
        return ClassroomMember.builder()
                .id(e.getId())
                .classroomId(ClassroomId.of(e.getClassroomId()))
                .userId(e.getUserId())
                .organizationId(e.getOrganizationId())
                .role(ClassroomMemberRole.valueOf(e.getRole()))
                .joinedAt(e.getJoinedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
