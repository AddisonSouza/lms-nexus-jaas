package br.edu.lms.module.storage.domain.model;

import lombok.Value;

/** Quem pede o arquivo, como o JWT o descreve. `organizationId` é nulo antes de escolher uma organização. */
@Value
public class FileRequester {
    String userId;
    String organizationId;
    String role;
}
