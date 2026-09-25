package br.edu.lms.module.storage.domain.port.out;

import br.edu.lms.module.storage.domain.model.FileRequester;
import br.edu.lms.module.storage.domain.model.StorageContext;

/**
 * Regra de leitura de um tipo de arquivo. Só o módulo dono do recurso sabe quem
 * pode vê-lo, então cada um implementa a do seu contexto; o storage apenas
 * pergunta antes de servir.
 */
public interface FileAccessPort {
    StorageContext context();

    /** `false` também quando a chave não pertence a nenhum recurso da organização do requester. */
    boolean canRead(String fileKey, FileRequester requester);
}
