package br.edu.lms.module.communication.interfaces.rest;

import br.edu.lms.module.communication.application.dto.NotificationListResponse;
import br.edu.lms.module.communication.application.dto.NotificationResponse;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.port.in.ListNotificationsUseCase;
import br.edu.lms.module.communication.domain.port.in.MarkAllNotificationsReadUseCase;
import br.edu.lms.module.communication.domain.port.in.MarkNotificationReadUseCase;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notificações in-app")
public class NotificationResource {

    private final ListNotificationsUseCase listNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;
    private final NotificationUnreadCounterPort notificationUnreadCounterPort;
    private final JsonWebToken jwt;

    @GET
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Listar notificações do usuário autenticado")
    public Response list() {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();

        var notifications = listNotificationsUseCase.execute(userId, orgId);
        long unreadCount = notificationUnreadCounterPort.get(userId);

        var response = NotificationListResponse.builder()
                .items(notifications.stream().map(NotificationResource::toResponse).toList())
                .unreadCount(unreadCount)
                .build();

        return Response.ok(response).build();
    }

    @PATCH
    @Path("/{id}/read")
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Marcar notificação como lida")
    public Response markRead(@PathParam("id") String id) {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();

        var notification = markNotificationReadUseCase.execute(id, userId, orgId);
        return Response.ok(toResponse(notification)).build();
    }

    @PATCH
    @Path("/read-all")
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Marcar todas as notificações do usuário autenticado como lidas")
    public Response markAllRead() {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();

        markAllNotificationsReadUseCase.execute(userId, orgId);
        long unreadCount = notificationUnreadCounterPort.get(userId);

        return Response.ok(Map.of("unreadCount", unreadCount)).build();
    }

    private static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId().getValue())
                .type(notification.getType().name())
                .referenceId(notification.getReferenceId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .actionLink(notification.getActionLink())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
