package br.edu.lms.shared.exception;

import br.edu.lms.module.assessment.domain.exception.DeadlineExpiredException;
import br.edu.lms.module.assessment.domain.exception.InvalidTaskStateException;
import br.edu.lms.module.assessment.domain.exception.SubmissionAlreadyEvaluatedException;
import br.edu.lms.module.assessment.domain.exception.SubmissionAlreadyExistsException;
import br.edu.lms.module.assessment.domain.exception.SubmissionNotFoundException;
import br.edu.lms.module.assessment.domain.exception.TaskNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.classroom.domain.exception.ClassroomArchivedException;
import br.edu.lms.module.curriculum.domain.exception.ContentAccessDeniedException;
import br.edu.lms.module.curriculum.domain.exception.ContentNotFoundException;
import br.edu.lms.module.curriculum.domain.exception.InvalidFileTypeException;
import br.edu.lms.module.curriculum.domain.exception.InvalidTeacherAssignmentException;
import br.edu.lms.module.curriculum.domain.exception.SubjectClassroomLinkNotFoundException;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.exception.SubjectTeacherAssignmentNotFoundException;
import br.edu.lms.module.curriculum.domain.exception.TopicNotFoundException;
import br.edu.lms.module.classroom.domain.exception.ClassroomMemberNotFoundException;
import br.edu.lms.module.classroom.domain.exception.ClassroomNotFoundException;
import br.edu.lms.module.classroom.domain.exception.InvalidInviteCodeException;
import br.edu.lms.module.classroom.domain.exception.MemberNotInOrganizationException;
import br.edu.lms.module.identity.domain.exception.EmailAlreadyConfirmedException;
import br.edu.lms.module.identity.domain.exception.EmailAlreadyInUseException;
import br.edu.lms.module.identity.domain.exception.InvalidConfirmationTokenException;
import br.edu.lms.module.identity.domain.exception.InvalidCredentialsException;
import br.edu.lms.module.identity.domain.exception.PasswordResetTokenInvalidException;
import br.edu.lms.module.identity.domain.exception.ResendRateLimitExceededException;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
import br.edu.lms.module.organization.domain.exception.AlreadyAMemberException;
import br.edu.lms.module.organization.domain.exception.CannotRemoveOwnerException;
import br.edu.lms.module.organization.domain.exception.InvitationAlreadyUsedException;
import br.edu.lms.module.organization.domain.exception.InvitationExpiredException;
import br.edu.lms.module.organization.domain.exception.InvitationNotFoundException;
import br.edu.lms.module.organization.domain.exception.MemberNotFoundException;
import br.edu.lms.module.organization.domain.exception.NotAnOrganizationMemberException;
import br.edu.lms.module.organization.domain.exception.OrganizationNameAlreadyExistsException;
import br.edu.lms.shared.domain.DomainException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof InvalidCredentialsException || exception instanceof TokenNotFoundException) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Unauthorized"))
                    .build();
        }

        if (exception instanceof PasswordResetTokenInvalidException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Token inválido, expirado ou já utilizado"))
                    .build();
        }

        if (exception instanceof InvalidConfirmationTokenException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "INVALID_CONFIRMATION_TOKEN"))
                    .build();
        }

        if (exception instanceof EmailAlreadyConfirmedException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "EMAIL_ALREADY_CONFIRMED"))
                    .build();
        }

        if (exception instanceof ResendRateLimitExceededException) {
            return Response.status(429)
                    .header("Retry-After", "3600")
                    .entity(Map.of("error", "RESEND_RATE_LIMIT_EXCEEDED"))
                    .build();
        }

        if (exception instanceof InvitationNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "INVITATION_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof InvitationExpiredException) {
            return Response.status(410)
                    .entity(Map.of("error", "INVITATION_EXPIRED"))
                    .build();
        }

        if (exception instanceof InvitationAlreadyUsedException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "INVITATION_ALREADY_USED"))
                    .build();
        }

        if (exception instanceof AlreadyAMemberException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "ALREADY_A_MEMBER"))
                    .build();
        }

        if (exception instanceof CannotRemoveOwnerException) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "CANNOT_REMOVE_OWNER"))
                    .build();
        }

        if (exception instanceof MemberNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "MEMBER_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof NotAnOrganizationMemberException) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "NOT_AN_ORGANIZATION_MEMBER"))
                    .build();
        }

        if (exception instanceof OrganizationNameAlreadyExistsException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "ORGANIZATION_NAME_ALREADY_EXISTS"))
                    .build();
        }

        if (exception instanceof InvalidInviteCodeException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "INVALID_INVITE_CODE"))
                    .build();
        }

        if (exception instanceof ClassroomNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "CLASSROOM_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof ClassroomMemberNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "CLASSROOM_MEMBER_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof MemberNotInOrganizationException) {
            return Response.status(422)
                    .entity(Map.of("error", "MEMBER_NOT_IN_ORGANIZATION"))
                    .build();
        }

        if (exception instanceof ClassroomArchivedException) {
            return Response.status(422)
                    .entity(Map.of("error", "CLASSROOM_ARCHIVED"))
                    .build();
        }

        if (exception instanceof SubjectNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "SUBJECT_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof TopicNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "TOPIC_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof ContentNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "CONTENT_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof ContentAccessDeniedException) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "CONTENT_ACCESS_DENIED"))
                    .build();
        }

        if (exception instanceof InvalidFileTypeException e) {
            return Response.status(422)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }

        if (exception instanceof SubjectClassroomLinkNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "SUBJECT_CLASSROOM_LINK_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof SubjectTeacherAssignmentNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "SUBJECT_TEACHER_ASSIGNMENT_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof TaskNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "TASK_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof SubmissionNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "SUBMISSION_NOT_FOUND"))
                    .build();
        }

        if (exception instanceof SubmissionAlreadyExistsException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "SUBMISSION_ALREADY_EXISTS"))
                    .build();
        }

        if (exception instanceof SubmissionAlreadyEvaluatedException e) {
            return Response.status(422)
                    .entity(Map.of("error", "SUBMISSION_ALREADY_EVALUATED"))
                    .build();
        }

        if (exception instanceof DeadlineExpiredException e) {
            return Response.status(422)
                    .entity(Map.of("error", "DEADLINE_EXPIRED"))
                    .build();
        }

        if (exception instanceof UnauthorizedTaskOperationException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "TASK_FORBIDDEN"))
                    .build();
        }

        if (exception instanceof InvalidTaskStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }

        if (exception instanceof IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }

        if (exception instanceof InvalidTeacherAssignmentException e) {
            return Response.status(422)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }

        if (exception instanceof EmailAlreadyInUseException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }

        if (exception instanceof ConstraintViolationException e) {
            var errors = e.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.toList());
            return Response.status(422)
                    .entity(Map.of("errors", errors))
                    .build();
        }

        if (exception instanceof DomainException e) {
            return Response.status(422)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Erro interno do servidor"))
                .build();
    }
}
