package br.edu.lms.module.communication.interfaces.rest;

import br.edu.lms.module.communication.application.dto.AnnouncementResponse;
import br.edu.lms.module.communication.application.dto.AttachmentInput;
import br.edu.lms.module.communication.application.dto.PostAnnouncementCommand;
import br.edu.lms.module.communication.domain.port.in.ListAnnouncementsUseCase;
import br.edu.lms.module.communication.domain.port.in.PostAnnouncementUseCase;
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
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Path("/classrooms")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Announcements", description = "Mural de avisos da turma")
public class ClassroomAnnouncementResource {

    private final PostAnnouncementUseCase postAnnouncementUseCase;
    private final ListAnnouncementsUseCase listAnnouncementsUseCase;
    private final JsonWebToken jwt;

    @POST
    @Path("/{classroomId}/announcements")
    @RolesAllowed("PROFESSOR")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Publicar aviso na turma")
    public Response create(
            @PathParam("classroomId") String classroomId,
            @FormParam("content") String content,
            @FormParam("externalUrl") String externalUrl,
            @FormParam("linkTitle") String linkTitle,
            @FormParam("files") List<FileUpload> files) throws IOException {

        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();

        var command = PostAnnouncementCommand.builder()
                .classroomId(classroomId)
                .organizationId(orgId)
                .authorId(userId)
                .content(content)
                .attachments(buildAttachments(files, externalUrl, linkTitle))
                .build();

        var response = postAnnouncementUseCase.execute(command);
        return Response.created(URI.create("/announcements/" + response.getId()))
                .entity(response).build();
    }

    @GET
    @Path("/{classroomId}/announcements")
    @RolesAllowed({"PROFESSOR", "ALUNO"})
    @Operation(summary = "Listar avisos da turma em ordem cronológica decrescente")
    public List<AnnouncementResponse> list(@PathParam("classroomId") String classroomId) {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();
        return listAnnouncementsUseCase.execute(classroomId, userId, orgId);
    }

    private List<AttachmentInput> buildAttachments(List<FileUpload> files, String externalUrl, String linkTitle) throws IOException {
        List<AttachmentInput> attachments = new ArrayList<>();
        if (files != null) {
            for (FileUpload file : files) {
                if (file != null && file.filePath() != null) {
                    InputStream stream = Files.newInputStream(file.filePath());
                    attachments.add(new AttachmentInput(stream, file.fileName(), file.contentType(), Files.size(file.filePath()), null, null));
                }
            }
        }
        if (externalUrl != null && !externalUrl.isBlank()) {
            attachments.add(new AttachmentInput(null, null, null, null, externalUrl, linkTitle));
        }
        return attachments;
    }
}
