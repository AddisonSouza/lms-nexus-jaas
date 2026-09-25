package br.edu.lms.module.storage.application.usecase;

import br.edu.lms.module.storage.domain.exception.FileNotFoundException;
import br.edu.lms.module.storage.domain.model.FileRequester;
import br.edu.lms.module.storage.domain.model.RetrievedFile;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.FileAccessPort;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ServeFileUseCase {

    private final StoragePort storagePort;
    private final Instance<FileAccessPort> accessPorts;

    /**
     * Negado vira 404, e não 403: responder diferente para "existe mas não é seu"
     * confirmaria a quem tem uma chave alheia que o arquivo está lá. Contexto sem
     * regra registrada também nega — o padrão é fechado.
     */
    public RetrievedFile execute(String fileKey, FileRequester requester) {
        var allowed = requester.getOrganizationId() != null
                && StorageContext.fromFileKey(fileKey)
                        .flatMap(context -> accessPorts.stream()
                                .filter(port -> port.context() == context)
                                .findFirst())
                        .map(port -> port.canRead(fileKey, requester))
                        .orElse(false);

        if (!allowed) {
            throw new FileNotFoundException(fileKey);
        }
        return storagePort.retrieve(fileKey);
    }
}
