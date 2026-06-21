package br.edu.lms.shared.exception;

import br.edu.lms.module.identity.domain.exception.ResendRateLimitExceededException;
import br.edu.lms.shared.domain.DomainException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ResendRateLimitExceededException e) {
            return Response.status(429)
                    .header("Retry-After", "3600")
                    .entity(Map.of("error", e.errorCode()))
                    .build();
        }

        if (exception instanceof HttpMappable e) {
            return Response.status(e.httpStatus())
                    .entity(Map.of("error", e.errorCode()))
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

        if (exception instanceof IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }

        // Preserve JAX-RS framework status codes (404 Not Found, 405, etc.)
        // instead of collapsing them into a generic 500.
        if (exception instanceof WebApplicationException e) {
            var status = e.getResponse().getStatusInfo();
            return Response.status(status)
                    .entity(Map.of("error", status.getReasonPhrase()))
                    .build();
        }

        LOG.error("Unhandled exception", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Erro interno do servidor"))
                .build();
    }
}
