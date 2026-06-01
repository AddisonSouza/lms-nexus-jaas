package br.edu.lms.module.identity.domain.model;

import br.edu.lms.shared.domain.DomainException;
import lombok.Value;

@Value
public class FullName {

    String value;

    public FullName(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("Nome completo não pode ser vazio");
        }
        if (value.trim().length() > 150) {
            throw new DomainException("Nome completo deve ter no máximo 150 caracteres");
        }
        this.value = value.trim();
    }
}
