package br.edu.lms.module.reporting.domain.model;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class DashboardPeriod {

    LocalDate from;
    LocalDate to;

    public DashboardPeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Período inválido: 'from' e 'to' são obrigatórios");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Período inválido: 'from' não pode ser posterior a 'to'");
        }
        this.from = from;
        this.to = to;
    }

    public LocalDateTime startInclusive() {
        return from.atStartOfDay();
    }

    public LocalDateTime endExclusive() {
        return to.plusDays(1).atStartOfDay();
    }
}
