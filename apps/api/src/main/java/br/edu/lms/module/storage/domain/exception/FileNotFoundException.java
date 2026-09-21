package br.edu.lms.module.storage.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

/**
 * Chave que não existe no storage. Sem isto o `NoSuchKeyException` do SDK subia
 * cru e virava 500: um anexo apagado parecia falha do servidor.
 */
public class FileNotFoundException extends RuntimeException implements HttpMappable {
    public FileNotFoundException(String fileKey) {
        super("File not found: " + fileKey);
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "FILE_NOT_FOUND"; }
}
