package br.edu.lms.shared.exception;

import br.edu.lms.module.identity.domain.exception.EmailAlreadyInUseException;
import br.edu.lms.module.identity.domain.exception.InvalidCredentialsException;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
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
