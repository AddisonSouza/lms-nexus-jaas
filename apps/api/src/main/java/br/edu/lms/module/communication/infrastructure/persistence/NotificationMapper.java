package br.edu.lms.module.communication.infrastructure.persistence;

import br.edu.lms.module.communication.domain.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface NotificationMapper {

    @Mapping(target = "id", expression = "java(br.edu.lms.module.communication.domain.model.NotificationId.of(entity.getId()))")
    @Mapping(target = "type", expression = "java(br.edu.lms.module.communication.domain.model.NotificationType.valueOf(entity.getType()))")
    Notification toDomain(NotificationJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "type", expression = "java(domain.getType().name())")
    NotificationJpaEntity toEntity(Notification domain);
}
