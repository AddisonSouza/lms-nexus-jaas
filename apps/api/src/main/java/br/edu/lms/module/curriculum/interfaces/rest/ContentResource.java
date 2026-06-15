package br.edu.lms.module.curriculum.interfaces.rest;

import br.edu.lms.module.curriculum.application.dto.CreateContentCommand;
import br.edu.lms.module.curriculum.application.dto.UpdateContentCommand;
import br.edu.lms.module.curriculum.domain.model.ContentType;
import br.edu.lms.module.curriculum.domain.port.in.CreateContentUseCase;
import br.edu.lms.module.curriculum.domain.port.in.DeleteContentUseCase;
import br.edu.lms.module.curriculum.domain.port.in.ListSubjectContentsUseCase;
import br.edu.lms.module.curriculum.domain.port.in.UpdateContentUseCase;
import br.edu.lms.module.curriculum.interfaces.rest.dto.UpdateContentRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
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

@Path("/subjects/{subjectId}/contents")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Contents", description = "Gestão de conteúdo complementar por disciplina")
public class ContentResource {

    private final CreateContentUseCase createContentUseCase;
    private final UpdateContentUseCase updateContentUseCase;
    private final DeleteContentUseCase deleteContentUseCase;
    private final ListSubjectContentsUseCase listSubjectContentsUseCase;
    private final JsonWebToken jwt;

    @GET
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Listar conteúdos agrupados por tópico")
    public Response list(@PathParam("subjectId") String subjectId) {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();
        String role = jwt.getGroups().stream().findFirst().orElse("ALUNO");
        return Response.ok(listSubjectContentsUseCase.execute(subjectId, orgId, userId, role)).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"PROFESSOR", "ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Criar conteúdo (multipart — arquivo opcional)")
    public Response create(
            @PathParam("subjectId") String subjectId,
            @FormParam("topicId") String topicId,
            @FormParam("title") String title,
            @FormParam("contentType") String contentType,
            @FormParam("externalUrl") String externalUrl,
            @FormParam("description") String description,
            @FormParam("file") FileUpload file) throws IOException {

        String orgId = (String) jwt.getClaim("org");

        InputStream fileStream = null;
        String fileName = null;
        String mimeType = null;
        long sizeBytes = 0;

        if (file != null && file.filePath() != null) {
            fileStream = Files.newInputStream(file.filePath());
            fileName = file.fileName();
            mimeType = file.contentType();
            sizeBytes = Files.size(file.filePath());
        }

        var command = CreateContentCommand.builder()
                .topicId(topicId)
                .subjectId(subjectId)
                .organizationId(orgId)
                .title(title)
                .contentType(ContentType.valueOf(contentType))
                .externalUrl(externalUrl)
                .description(description)
                .fileStream(fileStream)
                .fileName(fileName)
                .fileMimeType(mimeType)
                .fileSizeBytes(sizeBytes)
                .build();

        var response = createContentUseCase.execute(command);
        return Response.created(URI.create("/subjects/" + subjectId + "/contents/" + response.getId()))
                .entity(response).build();
    }

    @PUT
    @Path("/{contentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"PROFESSOR", "ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Atualizar conteúdo")
    public Response update(
            @PathParam("subjectId") String subjectId,
            @PathParam("contentId") String contentId,
            @Valid UpdateContentRequest req) {
        String orgId = (String) jwt.getClaim("org");
        var response = updateContentUseCase.execute(
                UpdateContentCommand.builder()
                        .contentId(contentId)
                        .subjectId(subjectId)
                        .organizationId(orgId)
                        .title(req.getTitle())
                        .description(req.getDescription())
                        .externalUrl(req.getExternalUrl())
                        .build());
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{contentId}")
    @RolesAllowed({"PROFESSOR", "ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Excluir conteúdo (soft delete)")
    public Response delete(@PathParam("subjectId") String subjectId, @PathParam("contentId") String contentId) {
        String orgId = (String) jwt.getClaim("org");
        deleteContentUseCase.execute(contentId, subjectId, orgId);
        return Response.noContent().build();
    }
}
