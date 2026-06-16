package br.edu.lms.module.assessment.interfaces.rest;

import br.edu.lms.module.assessment.application.dto.EvaluateSubmissionCommand;
import br.edu.lms.module.assessment.application.dto.SubmissionResponse;
import br.edu.lms.module.assessment.domain.port.in.EvaluateSubmissionUseCase;
import br.edu.lms.module.assessment.domain.port.in.GetSubmissionFeedbackUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;

@Path("/submissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Avaliação de submissões")
public class SubmissionResource {

    private final EvaluateSubmissionUseCase evaluateSubmissionUseCase;
    private final GetSubmissionFeedbackUseCase getSubmissionFeedbackUseCase;
    private final JsonWebToken jwt;

    @PATCH
    @Path("/{id}/evaluation")
    @RolesAllowed("PROFESSOR")
    @Operation(summary = "Avaliar uma submissão (professor)")
    public SubmissionResponse evaluate(
            @PathParam("id") String submissionId,
            EvaluationRequest body) {

        String orgId = (String) jwt.getClaim("org");
        String professorId = jwt.getSubject();

        var command = EvaluateSubmissionCommand.builder()
                .submissionId(submissionId)
                .professorId(professorId)
                .organizationId(orgId)
                .grade(body.grade())
                .feedback(body.feedback())
                .build();

        return evaluateSubmissionUseCase.execute(command);
    }

    @GET
    @Path("/{id}/feedback")
    @RolesAllowed("ALUNO")
    @Operation(summary = "Visualizar feedback de uma submissão avaliada (aluno)")
    public SubmissionResponse getFeedback(@PathParam("id") String submissionId) {
        String studentId = jwt.getSubject();
        String orgId = (String) jwt.getClaim("org");
        return getSubmissionFeedbackUseCase.execute(submissionId, studentId, orgId);
    }

    public record EvaluationRequest(BigDecimal grade, String feedback) {}
}
