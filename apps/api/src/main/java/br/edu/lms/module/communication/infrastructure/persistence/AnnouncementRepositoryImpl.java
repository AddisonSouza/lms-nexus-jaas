package br.edu.lms.module.communication.infrastructure.persistence;

import br.edu.lms.module.communication.domain.model.Announcement;
import br.edu.lms.module.communication.domain.model.AnnouncementId;
import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class AnnouncementRepositoryImpl implements AnnouncementRepository {

    private final EntityManager em;
    private final AnnouncementMapper announcementMapper;

    @Override
    @Transactional
    public Announcement save(Announcement announcement) {
        var entity = toEntityWithAttachments(announcement);
        var managed = em.merge(entity);
        em.flush();
        return announcementMapper.toDomain(managed);
    }

    @Override
    @Transactional
    public Optional<Announcement> findById(AnnouncementId id, String organizationId) {
        var entity = em.find(AnnouncementJpaEntity.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null || !entity.getOrganizationId().equals(organizationId)) {
            return Optional.empty();
        }
        return Optional.of(announcementMapper.toDomain(entity));
    }

    @Override
    @Transactional
    public List<Announcement> findByClassroomOrderByCreatedAtDesc(String classroomId, String organizationId) {
        TypedQuery<AnnouncementJpaEntity> q = em.createQuery(
                "SELECT a FROM AnnouncementJpaEntity a WHERE a.classroomId = :classroomId " +
                        "AND a.organizationId = :orgId AND a.deletedAt IS NULL ORDER BY a.createdAt DESC",
                AnnouncementJpaEntity.class);
        q.setParameter("classroomId", classroomId);
        q.setParameter("orgId", organizationId);
        return q.getResultList().stream().map(announcementMapper::toDomain).toList();
    }

    private AnnouncementJpaEntity toEntityWithAttachments(Announcement announcement) {
        var entity = announcementMapper.toEntity(announcement);

        if (announcement.getAttachments() != null) {
            var attachmentEntities = announcement.getAttachments().stream()
                    .map(a -> {
                        var ae = new AnnouncementAttachmentJpaEntity();
                        ae.setId(a.id());
                        ae.setAnnouncement(entity);
                        ae.setFileKey(a.fileKey());
                        ae.setOriginalName(a.originalName());
                        ae.setMimeType(a.mimeType());
                        ae.setSizeBytes(a.sizeBytes());
                        ae.setExternalUrl(a.externalUrl());
                        ae.setLinkTitle(a.linkTitle());
                        return ae;
                    }).toList();
            entity.setAttachments(attachmentEntities);
        }
        return entity;
    }
}
