package br.edu.lms.module.storage.interfaces.rest;

import br.edu.lms.module.storage.application.usecase.ServeFileUseCase;
import br.edu.lms.module.storage.domain.model.FileRequester;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Path("/files")
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Storage")
public class FileResource {

    private final ServeFileUseCase serveFileUseCase;
    private final JsonWebToken jwt;

    @GET
    @Path("/{fileKey:.+}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Serve stored file by key")
    @APIResponse(responseCode = "200", description = "Arquivo, com o tipo e o nome originais")
    @APIResponse(responseCode = "404", description = "Chave inexistente ou sem acesso ao recurso dono do arquivo")
    public Response getFile(@PathParam("fileKey") String fileKey) {
        var requester = new FileRequester(
                jwt.getSubject(),
                jwt.getClaim("org"),
                jwt.getGroups().stream().findFirst().orElse(null));
        var file = serveFileUseCase.execute(fileKey, requester);
        var metadata = file.getMetadata();

        var mimeType = metadata.getMimeType() != null
                ? metadata.getMimeType()
                : MediaType.APPLICATION_OCTET_STREAM;

        return Response.ok(file.getContent(), mimeType)
                .header("Content-Disposition", contentDisposition(metadata.getOriginalName()))
                .build();
    }

    /**
     * `filename` cobre os clientes antigos com um nome só-ASCII; `filename*`
     * (RFC 5987) leva o nome real, para que acento e espaço não virem lixo no
     * arquivo salvo.
     */
    private String contentDisposition(String originalName) {
        var fallback = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        var encoded = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"%s\"; filename*=UTF-8''%s".formatted(fallback, encoded);
    }
}
