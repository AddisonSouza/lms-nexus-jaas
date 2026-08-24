package br.edu.lms.module.classroom.interfaces.rest;

import br.edu.lms.module.classroom.application.dto.AddClassroomMemberCommand;
import br.edu.lms.module.classroom.application.dto.CreateClassroomCommand;
import br.edu.lms.module.classroom.application.dto.JoinClassroomCommand;
import br.edu.lms.module.classroom.application.dto.UpdateClassroomCommand;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.port.in.AddClassroomMemberUseCase;
import br.edu.lms.module.classroom.domain.port.in.CreateClassroomUseCase;
import br.edu.lms.module.classroom.domain.port.in.DeleteClassroomUseCase;
import br.edu.lms.module.classroom.domain.port.in.GetClassroomUseCase;
import br.edu.lms.module.classroom.domain.port.in.JoinClassroomUseCase;
import br.edu.lms.module.classroom.domain.port.in.ListClassroomMembersUseCase;
import br.edu.lms.module.classroom.domain.port.in.ListClassroomsUseCase;
import br.edu.lms.module.classroom.domain.port.in.RemoveClassroomMemberUseCase;
import br.edu.lms.module.classroom.domain.port.in.UpdateClassroomUseCase;
import br.edu.lms.module.classroom.interfaces.rest.dto.AddMemberRequest;
import br.edu.lms.module.classroom.interfaces.rest.dto.CreateClassroomRequest;
import br.edu.lms.module.classroom.interfaces.rest.dto.JoinClassroomRequest;
import br.edu.lms.module.classroom.interfaces.rest.dto.UpdateClassroomRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;

@Path("/classrooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Classrooms", description = "Gestão de turmas")
public class ClassroomResource {

    private final CreateClassroomUseCase createClassroomUseCase;
    private final UpdateClassroomUseCase updateClassroomUseCase;
    private final DeleteClassroomUseCase deleteClassroomUseCase;
    private final GetClassroomUseCase getClassroomUseCase;
    private final ListClassroomsUseCase listClassroomsUseCase;
    private final AddClassroomMemberUseCase addClassroomMemberUseCase;
    private final RemoveClassroomMemberUseCase removeClassroomMemberUseCase;
    private final ListClassroomMembersUseCase listClassroomMembersUseCase;
    private final JoinClassroomUseCase joinClassroomUseCase;
    private final JsonWebToken jwt;

    @GET
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Listar turmas")
    @APIResponse(responseCode = "200", description = "Lista de turmas")
    public Response list() {
        var orgId = (String) jwt.getClaim("org");
        var userId = jwt.getSubject();
        var role = primaryRole();
        return Response.ok(listClassroomsUseCase.execute(orgId, userId, role)).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Detalhar turma")
    @APIResponse(responseCode = "200", description = "Turma encontrada")
    @APIResponse(responseCode = "404", description = "Turma não encontrada")
    public Response getById(@PathParam("id") String id) {
        var orgId = (String) jwt.getClaim("org");
        var userId = jwt.getSubject();
        var role = primaryRole();
        return Response.ok(getClassroomUseCase.execute(ClassroomId.of(id), userId, orgId, role)).build();
    }

    @POST
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Criar turma")
    @APIResponse(responseCode = "201", description = "Turma criada")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Response create(@Valid CreateClassroomRequest request) {
        var orgId = (String) jwt.getClaim("org");
        var result = createClassroomUseCase.execute(
                CreateClassroomCommand.builder()
                        .name(request.name())
                        .description(request.description())
                        .academicPeriod(request.academicPeriod())
                        .organizationId(orgId)
                        .build());
        return Response.created(URI.create("/classrooms/" + result.getId())).entity(result).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Atualizar turma")
    @APIResponse(responseCode = "200", description = "Turma atualizada")
    @APIResponse(responseCode = "404", description = "Turma não encontrada")
    public Response update(@PathParam("id") String id, @Valid UpdateClassroomRequest request) {
        var orgId = (String) jwt.getClaim("org");
        var result = updateClassroomUseCase.execute(
                ClassroomId.of(id),
                UpdateClassroomCommand.builder()
                        .name(request.name())
                        .description(request.description())
                        .academicPeriod(request.academicPeriod())
                        .status(request.status())
                        .organizationId(orgId)
                        .build());
        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Excluir turma (soft delete)")
    @APIResponse(responseCode = "204", description = "Turma excluída")
    @APIResponse(responseCode = "404", description = "Turma não encontrada")
    public Response delete(@PathParam("id") String id) {
        var orgId = (String) jwt.getClaim("org");
        deleteClassroomUseCase.execute(ClassroomId.of(id), orgId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/members")
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Listar membros da turma")
    @APIResponse(responseCode = "200", description = "Lista de membros")
    public Response listMembers(@PathParam("id") String id) {
        var orgId = (String) jwt.getClaim("org");
        return Response.ok(listClassroomMembersUseCase.execute(ClassroomId.of(id), orgId)).build();
    }

    @POST
    @Path("/{id}/members")
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Adicionar membro à turma")
    @APIResponse(responseCode = "201", description = "Membro adicionado")
    @APIResponse(responseCode = "422", description = "Usuário não pertence à organização ou turma arquivada")
    public Response addMember(@PathParam("id") String id, @Valid AddMemberRequest request) {
        var orgId = (String) jwt.getClaim("org");
        var result = addClassroomMemberUseCase.execute(
                AddClassroomMemberCommand.builder()
                        .classroomId(ClassroomId.of(id))
                        .userId(request.userId())
                        .organizationId(orgId)
                        .role(request.role())
                        .build());
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @DELETE
    @Path("/{id}/members/{userId}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR"})
    @Operation(summary = "Remover membro da turma")
    @APIResponse(responseCode = "204", description = "Membro removido")
    @APIResponse(responseCode = "404", description = "Membro não encontrado")
    public Response removeMember(@PathParam("id") String id, @PathParam("userId") String userId) {
        var orgId = (String) jwt.getClaim("org");
        removeClassroomMemberUseCase.execute(ClassroomId.of(id), userId, orgId);
        return Response.noContent().build();
    }

    @POST
    @Path("/join")
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Ingressar em turma via código de convite")
    @APIResponse(responseCode = "201", description = "Ingresso realizado com sucesso")
    @APIResponse(responseCode = "200", description = "Usuário já era membro da turma")
    @APIResponse(responseCode = "404", description = "Código inválido")
    @APIResponse(responseCode = "422", description = "Turma arquivada")
    public Response join(@Valid JoinClassroomRequest request) {
        var userId = jwt.getSubject();
        var result = joinClassroomUseCase.execute(
                JoinClassroomCommand.builder()
                        .inviteCode(request.inviteCode())
                        .userId(userId)
                        .build());
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    private String primaryRole() {
        var groups = jwt.getGroups();
        if (groups == null) return "ALUNO";
        if (groups.contains("ADMIN_ORG")) return "ADMIN_ORG";
        if (groups.contains("GESTOR")) return "GESTOR";
        if (groups.contains("PROFESSOR")) return "PROFESSOR";
        return "ALUNO";
    }
}
