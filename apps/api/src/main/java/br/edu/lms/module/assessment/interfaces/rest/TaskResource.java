package br.edu.lms.module.assessment.interfaces.rest;

import br.edu.lms.module.assessment.application.dto.AttachmentInput;
import br.edu.lms.module.assessment.application.dto.CreateTaskCommand;
import br.edu.lms.module.assessment.application.dto.EditSubmissionCommand;
import br.edu.lms.module.assessment.application.dto.SubmissionResponse;
import br.edu.lms.module.assessment.application.dto.SubmitTaskCommand;
import br.edu.lms.module.assessment.application.dto.TaskResponse;
import br.edu.lms.module.assessment.application.dto.TaskWithGradeResponse;
import br.edu.lms.module.assessment.domain.port.in.CreateTaskUseCase;
import br.edu.lms.module.assessment.domain.port.in.EditSubmissionUseCase;
import br.edu.lms.module.assessment.domain.port.in.ListPublishedTasksUseCase;
import br.edu.lms.module.assessment.domain.port.in.ListStudentGradesUseCase;
import br.edu.lms.module.assessment.domain.port.in.ListTaskSubmissionsUseCase;
import br.edu.lms.module.assessment.domain.port.in.ListTasksUseCase;
import br.edu.lms.module.assessment.domain.port.in.PublishTaskUseCase;
import br.edu.lms.module.assessment.domain.port.in.SubmitTaskUseCase;
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
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Gestão de tarefas avaliativas")
public class TaskResource {

    private final CreateTaskUseCase createTaskUseCase;
    private final PublishTaskUseCase publishTaskUseCase;
    private final SubmitTaskUseCase submitTaskUseCase;
    private final EditSubmissionUseCase editSubmissionUseCase;
    private final ListStudentGradesUseCase listStudentGradesUseCase;
    private final ListTaskSubmissionsUseCase listTaskSubmissionsUseCase;
    private final ListTasksUseCase listTasksUseCase;
    private final ListPublishedTasksUseCase listPublishedTasksUseCase;
    private final JsonWebToken jwt;

    @GET
    @RolesAllowed("PROFESSOR")
    @Operation(summary = "Listar tarefas do professor na organização")
    public List<TaskResponse> list() {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();
        return listTasksUseCase.execute(orgId, userId);
    }

    @GET
    @Path("/published")
    @RolesAllowed("ALUNO")
    @Operation(summary = "Listar tarefas publicadas (aluno)")
    public List<TaskResponse> listPublished() {
        String orgId = (String) jwt.getClaim("org");
        return listPublishedTasksUseCase.execute(orgId);
    }

    @GET
    @Path("/my-grades")
    @RolesAllowed("ALUNO")
    @Operation(summary = "Listar tarefas com notas e feedback do aluno")
    public List<TaskWithGradeResponse> myGrades() {
        String orgId = (String) jwt.getClaim("org");
        String studentId = jwt.getSubject();
        return listStudentGradesUseCase.execute(studentId, orgId);
    }

    @POST
    @RolesAllowed("PROFESSOR")
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
    @RolesAllowed("PROFESSOR")
    @Operation(summary = "Publicar tarefa (DRAFT → PUBLISHED)")
    public Response publish(@PathParam("taskId") String taskId) {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();
        var response = publishTaskUseCase.execute(taskId, orgId, userId);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{taskId}/submissions")
    @RolesAllowed("ALUNO")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Enviar resposta para uma tarefa (aluno)")
    public Response submit(
            @PathParam("taskId") String taskId,
            @FormParam("textResponse") String textResponse,
            @FormParam("files") List<FileUpload> files) throws IOException {

        String orgId = (String) jwt.getClaim("org");
        String studentId = jwt.getSubject();

        List<AttachmentInput> attachments = buildAttachments(files);

        var command = SubmitTaskCommand.builder()
                .taskId(taskId)
                .studentId(studentId)
                .organizationId(orgId)
                .textResponse(textResponse)
                .attachments(attachments)
                .build();

        SubmissionResponse response = submitTaskUseCase.execute(command);
        return Response.created(URI.create("/tasks/" + taskId + "/submissions/" + response.getId()))
                .entity(response).build();
    }

    @PUT
    @Path("/{taskId}/submissions/{submissionId}")
    @RolesAllowed("ALUNO")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Editar resposta enviada (aluno)")
    public Response editSubmission(
            @PathParam("taskId") String taskId,
            @PathParam("submissionId") String submissionId,
            @FormParam("textResponse") String textResponse,
            @FormParam("files") List<FileUpload> files) throws IOException {

        String studentId = jwt.getSubject();

        List<AttachmentInput> attachments = buildAttachments(files);

        var command = EditSubmissionCommand.builder()
                .submissionId(submissionId)
                .taskId(taskId)
                .studentId(studentId)
                .textResponse(textResponse)
                .attachments(attachments)
                .build();

        SubmissionResponse response = editSubmissionUseCase.execute(command);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{taskId}/submissions")
    @RolesAllowed("PROFESSOR")
    @Operation(summary = "Listar submissões de uma tarefa (professor)")
    public List<SubmissionResponse> listSubmissions(@PathParam("taskId") String taskId) {
        String orgId = (String) jwt.getClaim("org");
        String userId = jwt.getSubject();
        return listTaskSubmissionsUseCase.execute(taskId, userId, orgId);
    }

    private List<AttachmentInput> buildAttachments(List<FileUpload> files) throws IOException {
        List<AttachmentInput> attachments = new ArrayList<>();
        if (files != null) {
            for (FileUpload file : files) {
                if (file != null && file.filePath() != null) {
                    InputStream stream = Files.newInputStream(file.filePath());
                    attachments.add(new AttachmentInput(stream, file.fileName(), file.contentType(), Files.size(file.filePath())));
                }
            }
        }
        return attachments;
    }
}
