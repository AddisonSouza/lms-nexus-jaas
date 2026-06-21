package br.edu.lms.shared.exception;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapeia falhas de Bean Validation em parâmetros de request (@Valid) para 422
 * (Unprocessable Entity), sobrepondo o mapper padrão do Quarkus que devolve 400.
 *
 * Violações em valor de retorno indicam bug no servidor → 500.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyReactiveViolationException> {

    @Override
    public Response toResponse(ResteasyReactiveViolationException exception) {
        boolean returnValueViolation = exception.getConstraintViolations().stream()
                .flatMap(v -> stream(v.getPropertyPath()))
                .anyMatch(node -> node.getKind() == ElementKind.RETURN_VALUE);

        if (returnValueViolation) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro interno do servidor"))
                    .build();
        }

        var errors = exception.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());
        return Response.status(422)
                .entity(Map.of("errors", errors))
                .build();
    }

    private static java.util.stream.Stream<Path.Node> stream(Path path) {
        return java.util.stream.StreamSupport.stream(path.spliterator(), false);
    }
}
