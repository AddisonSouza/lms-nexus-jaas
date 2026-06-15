package br.edu.lms.module.storage.domain.port.out;

import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.model.StoredFile;

import java.io.InputStream;

public interface StoragePort {
    StoredFile store(InputStream content, String filename, String mimeType, long sizeBytes, StorageContext context);
    InputStream retrieve(String fileKey);
    void delete(String fileKey);
    String getPublicUrl(String fileKey);
}
