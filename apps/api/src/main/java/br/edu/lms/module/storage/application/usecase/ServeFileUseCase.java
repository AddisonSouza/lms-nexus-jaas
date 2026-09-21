package br.edu.lms.module.storage.application.usecase;

import br.edu.lms.module.storage.domain.model.RetrievedFile;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ServeFileUseCase {

    private final StoragePort storagePort;

    public RetrievedFile execute(String fileKey) {
        return storagePort.retrieve(fileKey);
    }
}
