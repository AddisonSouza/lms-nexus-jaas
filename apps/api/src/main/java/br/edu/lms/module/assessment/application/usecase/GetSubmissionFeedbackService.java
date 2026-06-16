package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.SubmissionResponse;
import br.edu.lms.module.assessment.domain.exception.SubmissionNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.SubmissionId;
import br.edu.lms.module.assessment.domain.model.SubmissionStatus;
import br.edu.lms.module.assessment.domain.port.in.GetSubmissionFeedbackUseCase;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class GetSubmissionFeedbackService implements GetSubmissionFeedbackUseCase {

    private final SubmissionRepository submissionRepository;

    @Override
    public SubmissionResponse execute(String submissionId, String studentId, String organizationId) {
        var submission = submissionRepository.findById(SubmissionId.of(submissionId))
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));

        if (!submission.getStudentId().equals(studentId) || !submission.getOrganizationId().equals(organizationId)) {
            throw new UnauthorizedTaskOperationException(studentId, submissionId);
        }

        if (submission.getStatus() != SubmissionStatus.EVALUATED) {
            throw new ClientErrorException("Submissão ainda não foi avaliada", 409);
        }

        return SubmitTaskService.toResponse(submission);
    }
}
