package br.edu.lms.module.classroom.infrastructure.persistence;

import br.edu.lms.module.classroom.domain.model.Classroom;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomMember;
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
    private final ClassroomMapper classroomMapper;

    @Override
    @Transactional
    public Classroom save(Classroom classroom) {
        var entity = classroomMapper.toEntity(classroom);
        em.merge(entity);
        return classroom;
    }

    @Override
    public Optional<Classroom> findById(ClassroomId id, String organizationId) {
        return Optional.ofNullable(em.find(ClassroomJpaEntity.class, id.getValue()))
                .filter(e -> e.getDeletedAt() == null && e.getOrganizationId().equals(organizationId))
                .map(classroomMapper::toDomain);
    }

    @Override
    public List<Classroom> findAllByOrganization(String organizationId) {
        return em.createQuery(
                        "SELECT c FROM ClassroomJpaEntity c WHERE c.organizationId = :orgId AND c.deletedAt IS NULL",
                        ClassroomJpaEntity.class)
                .setParameter("orgId", organizationId)
                .getResultList()
                .stream()
                .map(classroomMapper::toDomain)
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
                .map(classroomMapper::toDomain)
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
                .map(classroomMapper::toMemberDomain);
    }

    @Override
    @Transactional
    public ClassroomMember saveMember(ClassroomMember member) {
        var entity = classroomMapper.toMemberEntity(member);
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
                .map(classroomMapper::toMemberDomain)
                .toList();
    }

    @Override
    public Optional<Classroom> findByInviteCode(String code, String organizationId) {
        return em.createQuery(
                        "SELECT c FROM ClassroomJpaEntity c " +
                        "WHERE c.inviteCode = :code AND c.organizationId = :orgId AND c.deletedAt IS NULL",
                        ClassroomJpaEntity.class)
                .setParameter("code", code)
                .setParameter("orgId", organizationId)
                .getResultStream()
                .findFirst()
                .map(classroomMapper::toDomain);
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
}
