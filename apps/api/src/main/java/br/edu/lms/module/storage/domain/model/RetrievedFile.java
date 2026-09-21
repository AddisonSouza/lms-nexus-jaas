package br.edu.lms.module.storage.domain.model;

import lombok.Value;

import java.io.InputStream;

/**
 * Conteúdo do arquivo junto dos metadados que a resposta HTTP precisa. Vêm da
 * mesma chamada ao storage: pedir o stream e depois consultar o tipo seria uma
 * ida a mais, e o `Content-Type` real é o que faz o navegador abrir o PDF como
 * PDF em vez de `application/octet-stream`.
 */
@Value
public class RetrievedFile {
    StoredFile metadata;
    InputStream content;
}
