package br.edu.lms.module.communication.interfaces.rest;

import br.edu.lms.module.communication.application.dto.EditAnnouncementCommand;
import br.edu.lms.module.communication.domain.port.in.DeleteAnnouncementUseCase;
import br.edu.lms.module.communication.domain.port.in.EditAnnouncementUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.util.List;

@Path("/announcements")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Announcements", description = "Mural de avisos da turma")
public class AnnouncementResource {

    private final EditAnnouncementUseCase editAnnouncementUseCase;
    private final DeleteAnnouncementUseCase deleteAnnouncementUseCase;
    private final JsonWebToken jwt;

    @PUT
    @Path("/{id}")
    @RolesAllowed("PROFESSOR")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Editar aviso (somente autor)")
    public Response update(
            @PathParam("id") String id,
            @FormParam("content") String content,
            @FormParam("externalUrl") String externalUrl,
            @FormParam("linkTitle") String linkTitle,
            @FormParam("files") List<FileUpload> files) throws IOException {

        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();

        boolean replaceAttachments = (files != null && !files.isEmpty()) || (externalUrl != null && !externalUrl.isBlank());

        var command = EditAnnouncementCommand.builder()
                .announcementId(id)
                .userId(userId)
                .organizationId(orgId)
                .content(content)
                .attachments(replaceAttachments ? AnnouncementAttachmentRequestMapper.buildAttachments(files, externalUrl, linkTitle) : null)
                .build();

        var response = editAnnouncementUseCase.execute(command);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("PROFESSOR")
    @Operation(summary = "Excluir aviso (soft delete, somente autor)")
    public Response delete(@PathParam("id") String id) {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();
        deleteAnnouncementUseCase.execute(id, userId, orgId);
        return Response.noContent().build();
    }
}
