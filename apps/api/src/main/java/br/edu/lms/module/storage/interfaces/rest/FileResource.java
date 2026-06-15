package br.edu.lms.module.storage.interfaces.rest;

import br.edu.lms.module.storage.application.usecase.ServeFileUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.io.InputStream;

@Path("/api/files")
@ApplicationScoped
@RequiredArgsConstructor
@Tag(name = "Storage")
public class FileResource {

    private final ServeFileUseCase serveFileUseCase;

    @GET
    @Path("/{fileKey:.+}")
    @RolesAllowed({"ADMIN_ORG", "GESTOR", "PROFESSOR", "ALUNO"})
    @Operation(summary = "Serve stored file by key")
    public Response getFile(@PathParam("fileKey") String fileKey) {
        InputStream stream = serveFileUseCase.execute(fileKey);
        return Response.ok(stream).build();
    }
}
