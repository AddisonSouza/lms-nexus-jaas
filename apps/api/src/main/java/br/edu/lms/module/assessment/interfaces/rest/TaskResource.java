package br.edu.lms.module.assessment.interfaces.rest;

import br.edu.lms.module.assessment.application.dto.AttachmentInput;
import br.edu.lms.module.assessment.application.dto.CreateTaskCommand;
import br.edu.lms.module.assessment.application.dto.TaskResponse;
import br.edu.lms.module.assessment.application.usecase.CreateTaskService;
import br.edu.lms.module.assessment.domain.port.in.CreateTaskUseCase;
import br.edu.lms.module.assessment.domain.port.in.PublishTaskUseCase;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("PROFESSOR")
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Gestão de tarefas avaliativas")
public class TaskResource {

    private final CreateTaskUseCase createTaskUseCase;
    private final PublishTaskUseCase publishTaskUseCase;
    private final TaskRepository taskRepository;
    private final JsonWebToken jwt;

    @GET
    @Operation(summary = "Listar tarefas do professor na organização")
    public List<TaskResponse> list() {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();
        return taskRepository.findByOrganizationAndCreatedBy(orgId, userId)
                .stream().map(CreateTaskService::toResponse).toList();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Criar tarefa em DRAFT")
    public Response create(
            @FormParam("subjectId") String subjectId,
            @FormParam("title") String title,
            @FormParam("description") String description,
            @FormParam("deadline") String deadline,
            @FormParam("maxScore") String maxScore,
            @FormParam("files") List<FileUpload> files) throws IOException {

        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();

        List<AttachmentInput> attachments = new ArrayList<>();
        if (files != null) {
            for (FileUpload file : files) {
                if (file != null && file.filePath() != null) {
                    InputStream stream = Files.newInputStream(file.filePath());
                    attachments.add(new AttachmentInput(
                            stream,
                            file.fileName(),
                            file.contentType(),
                            Files.size(file.filePath())));
                }
            }
        }

        var command = CreateTaskCommand.builder()
                .subjectId(subjectId)
                .organizationId(orgId)
                .createdBy(userId)
                .title(title)
                .description(description)
                .deadline(LocalDateTime.parse(deadline))
                .maxScore(maxScore != null && !maxScore.isBlank() ? new BigDecimal(maxScore) : null)
                .attachments(attachments)
                .build();

        var response = createTaskUseCase.execute(command);
        return Response.created(URI.create("/tasks/" + response.getId()))
                .entity(response).build();
    }

    @PATCH
    @Path("/{taskId}/publish")
    @Operation(summary = "Publicar tarefa (DRAFT → PUBLISHED)")
    public Response publish(@PathParam("taskId") String taskId) {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();
        var response = publishTaskUseCase.execute(taskId, orgId, userId);
        return Response.ok(response).build();
    }
}
