package br.edu.lms.shared.domain;

import java.util.List;

/**
 * Uma página de resultados, no formato que o contrato de API define para
 * listagens: {@code { content, totalElements, totalPages, number, size }}.
 */
public record Page<T>(List<T> content, long totalElements, int totalPages, int number, int size) {

    public static <T> Page<T> of(List<T> content, long totalElements, int number, int size) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new Page<>(List.copyOf(content), totalElements, totalPages, number, size);
    }
}
