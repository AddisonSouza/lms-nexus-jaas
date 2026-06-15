package br.edu.lms.module.curriculum.interfaces.rest;

import br.edu.lms.module.curriculum.application.dto.CreateTopicCommand;
import br.edu.lms.module.curriculum.application.dto.ReorderTopicsCommand;
import br.edu.lms.module.curriculum.application.dto.UpdateTopicCommand;
import br.edu.lms.module.curriculum.domain.port.in.CreateTopicUseCase;
import br.edu.lms.module.curriculum.domain.port.in.DeleteTopicUseCase;
import br.edu.lms.module.curriculum.domain.port.in.ListTopicsUseCase;
import br.edu.lms.module.curriculum.domain.port.in.ReorderTopicsUseCase;
import br.edu.lms.module.curriculum.domain.port.in.UpdateTopicUseCase;
import br.edu.lms.module.curriculum.interfaces.rest.dto.CreateTopicRequest;
import br.edu.lms.module.curriculum.interfaces.rest.dto.ReorderTopicsRequest;
import br.edu.lms.module.curriculum.interfaces.rest.dto.UpdateTopicRequest;
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

import java.net.URI;

@Path("/subjects/{subjectId}/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Topics", description = "Gestão de tópicos por disciplina")
public class TopicResource {

    private final CreateTopicUseCase createTopicUseCase;
    private final UpdateTopicUseCase updateTopicUseCase;
    private final DeleteTopicUseCase deleteTopicUseCase;
    private final ReorderTopicsUseCase reorderTopicsUseCase;
    private final ListTopicsUseCase listTopicsUseCase;
    private final JsonWebToken jwt;

    @GET
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Listar tópicos da disciplina")
    public Response list(@PathParam("subjectId") String subjectId) {
        String orgId = (String) jwt.getClaim("org");
        return Response.ok(listTopicsUseCase.execute(subjectId, orgId)).build();
    }

    @POST
    @RolesAllowed({"PROFESSOR", "ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Criar tópico")
    public Response create(@PathParam("subjectId") String subjectId, @Valid CreateTopicRequest req) {
        String orgId = (String) jwt.getClaim("org");
        var response = createTopicUseCase.execute(
                CreateTopicCommand.builder()
                        .subjectId(subjectId)
                        .organizationId(orgId)
                        .title(req.getTitle())
                        .build());
        return Response.created(URI.create("/subjects/" + subjectId + "/topics/" + response.getId()))
                .entity(response).build();
    }

    @PUT
    @Path("/{topicId}")
    @RolesAllowed({"PROFESSOR", "ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Atualizar tópico")
    public Response update(@PathParam("subjectId") String subjectId, @PathParam("topicId") String topicId, @Valid UpdateTopicRequest req) {
        String orgId = (String) jwt.getClaim("org");
        var response = updateTopicUseCase.execute(
                UpdateTopicCommand.builder()
                        .topicId(topicId)
                        .subjectId(subjectId)
                        .organizationId(orgId)
                        .title(req.getTitle())
                        .build());
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{topicId}")
    @RolesAllowed({"PROFESSOR", "ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Excluir tópico (soft delete)")
    public Response delete(@PathParam("subjectId") String subjectId, @PathParam("topicId") String topicId) {
        String orgId = (String) jwt.getClaim("org");
        deleteTopicUseCase.execute(topicId, subjectId, orgId);
        return Response.noContent().build();
    }

    @PUT
    @Path("/reorder")
    @RolesAllowed({"PROFESSOR", "ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Reordenar tópicos")
    public Response reorder(@PathParam("subjectId") String subjectId, @Valid ReorderTopicsRequest req) {
        String orgId = (String) jwt.getClaim("org");
        var response = reorderTopicsUseCase.execute(
                ReorderTopicsCommand.builder()
                        .subjectId(subjectId)
                        .organizationId(orgId)
                        .topicIds(req.getTopicIds())
                        .build());
        return Response.ok(response).build();
    }
}
