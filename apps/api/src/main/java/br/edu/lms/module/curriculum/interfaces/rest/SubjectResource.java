package br.edu.lms.module.curriculum.interfaces.rest;

import br.edu.lms.module.curriculum.application.dto.AssignTeacherCommand;
import br.edu.lms.module.curriculum.application.dto.CreateSubjectCommand;
import br.edu.lms.module.curriculum.application.dto.LinkClassroomCommand;
import br.edu.lms.module.curriculum.application.dto.UpdateSubjectCommand;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.AssignTeacherToSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.in.CreateSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.in.DeleteSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.in.GetSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.in.LinkSubjectToClassroomUseCase;
import br.edu.lms.module.curriculum.domain.port.in.ListSubjectsUseCase;
import br.edu.lms.module.curriculum.domain.port.in.RemoveTeacherFromSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.in.UnlinkSubjectFromClassroomUseCase;
import br.edu.lms.module.curriculum.domain.port.in.UpdateSubjectUseCase;
import br.edu.lms.module.curriculum.interfaces.rest.dto.AssignTeacherRequest;
import br.edu.lms.module.curriculum.interfaces.rest.dto.CreateSubjectRequest;
import br.edu.lms.module.curriculum.interfaces.rest.dto.LinkClassroomRequest;
import br.edu.lms.module.curriculum.interfaces.rest.dto.UpdateSubjectRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;

@Path("/subjects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Gestão de disciplinas")
public class SubjectResource {

    private final CreateSubjectUseCase createSubjectUseCase;
    private final UpdateSubjectUseCase updateSubjectUseCase;
    private final DeleteSubjectUseCase deleteSubjectUseCase;
    private final GetSubjectUseCase getSubjectUseCase;
    private final ListSubjectsUseCase listSubjectsUseCase;
    private final LinkSubjectToClassroomUseCase linkSubjectToClassroomUseCase;
    private final UnlinkSubjectFromClassroomUseCase unlinkSubjectFromClassroomUseCase;
    private final AssignTeacherToSubjectUseCase assignTeacherToSubjectUseCase;
    private final RemoveTeacherFromSubjectUseCase removeTeacherFromSubjectUseCase;
    private final JsonWebToken jwt;

    @GET
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR"})
    @Operation(summary = "Listar disciplinas")
    @APIResponse(responseCode = "200", description = "Lista de disciplinas")
    public Response list() {
        var orgId = (String) jwt.getClaim("org");
        return Response.ok(listSubjectsUseCase.execute(orgId)).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR"})
    @Operation(summary = "Detalhar disciplina")
    @APIResponse(responseCode = "200", description = "Disciplina encontrada")
    @APIResponse(responseCode = "404", description = "Disciplina não encontrada")
    public Response getById(@PathParam("id") String id) {
        var orgId = (String) jwt.getClaim("org");
        return Response.ok(getSubjectUseCase.execute(SubjectId.of(id), orgId)).build();
    }

    @POST
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Criar disciplina")
    @APIResponse(responseCode = "201", description = "Disciplina criada")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Response create(@Valid CreateSubjectRequest request) {
        var orgId = (String) jwt.getClaim("org");
        var result = createSubjectUseCase.execute(
                CreateSubjectCommand.builder()
                        .name(request.name())
                        .code(request.code())
                        .description(request.description())
                        .workloadHours(request.workloadHours())
                        .organizationId(orgId)
                        .build());
        return Response.created(URI.create("/subjects/" + result.getId())).entity(result).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Atualizar disciplina")
    @APIResponse(responseCode = "200", description = "Disciplina atualizada")
    @APIResponse(responseCode = "404", description = "Disciplina não encontrada")
    public Response update(@PathParam("id") String id, @Valid UpdateSubjectRequest request) {
        var orgId = (String) jwt.getClaim("org");
        var result = updateSubjectUseCase.execute(
                SubjectId.of(id),
                UpdateSubjectCommand.builder()
                        .name(request.name())
                        .code(request.code())
                        .description(request.description())
                        .workloadHours(request.workloadHours())
                        .organizationId(orgId)
                        .build());
        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN_ORG"})
    @Operation(summary = "Excluir disciplina (soft delete)")
    @APIResponse(responseCode = "204", description = "Disciplina excluída")
    @APIResponse(responseCode = "404", description = "Disciplina não encontrada")
    public Response delete(@PathParam("id") String id) {
        var orgId = (String) jwt.getClaim("org");
        deleteSubjectUseCase.execute(SubjectId.of(id), orgId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/classrooms")
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Vincular turma à disciplina")
    @APIResponse(responseCode = "201", description = "Turma vinculada")
    @APIResponse(responseCode = "200", description = "Vínculo já existia (idempotente)")
    @APIResponse(responseCode = "404", description = "Disciplina ou turma não encontrada")
    @APIResponse(responseCode = "422", description = "Turma arquivada")
    public Response linkClassroom(@PathParam("id") String id, @Valid LinkClassroomRequest request) {
        var orgId = (String) jwt.getClaim("org");
        boolean created = linkSubjectToClassroomUseCase.execute(
                SubjectId.of(id),
                LinkClassroomCommand.builder()
                        .classroomId(request.classroomId())
                        .organizationId(orgId)
                        .build());
        return created ? Response.status(Response.Status.CREATED).build() : Response.ok().build();
    }

    @DELETE
    @Path("/{id}/classrooms/{classroomId}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Desvincular turma da disciplina")
    @APIResponse(responseCode = "204", description = "Turma desvinculada")
    @APIResponse(responseCode = "404", description = "Vínculo não encontrado")
    public Response unlinkClassroom(@PathParam("id") String id, @PathParam("classroomId") String classroomId) {
        var orgId = (String) jwt.getClaim("org");
        unlinkSubjectFromClassroomUseCase.execute(SubjectId.of(id), classroomId, orgId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/teachers")
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Atribuir professor à disciplina")
    @APIResponse(responseCode = "201", description = "Professor atribuído")
    @APIResponse(responseCode = "200", description = "Professor já atribuído (idempotente)")
    @APIResponse(responseCode = "404", description = "Disciplina não encontrada")
    @APIResponse(responseCode = "422", description = "Membro não encontrado na organização ou não é PROFESSOR")
    public Response assignTeacher(@PathParam("id") String id, @Valid AssignTeacherRequest request) {
        var orgId = (String) jwt.getClaim("org");
        boolean created = assignTeacherToSubjectUseCase.execute(
                SubjectId.of(id),
                AssignTeacherCommand.builder()
                        .memberId(request.memberId())
                        .organizationId(orgId)
                        .build());
        return created ? Response.status(Response.Status.CREATED).build() : Response.ok().build();
    }

    @DELETE
    @Path("/{id}/teachers/{memberId}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Remover professor da disciplina")
    @APIResponse(responseCode = "204", description = "Professor removido")
    @APIResponse(responseCode = "404", description = "Atribuição não encontrada")
    public Response removeTeacher(@PathParam("id") String id, @PathParam("memberId") String memberId) {
        var orgId = (String) jwt.getClaim("org");
        removeTeacherFromSubjectUseCase.execute(SubjectId.of(id), memberId, orgId);
        return Response.noContent().build();
    }
}
