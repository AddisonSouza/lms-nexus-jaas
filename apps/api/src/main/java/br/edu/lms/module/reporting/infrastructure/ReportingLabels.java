package br.edu.lms.module.reporting.infrastructure;

import java.util.Map;

/**
 * Rótulos legíveis dos enums exibidos nos relatórios (feed de atividades e PDF).
 * Chave desconhecida deve ser exibida como veio.
 */
public final class ReportingLabels {

    public static final Map<String, String> ROLES = Map.of(
            "ADMIN_ORG", "Administrador",
            "GESTOR", "Gestor",
            "PROFESSOR", "Professor",
            "ALUNO", "Aluno");

    public static final Map<String, String> CLASSROOM_STATUSES = Map.of(
            "ACTIVE", "Ativa",
            "ARCHIVED", "Arquivada");

    private ReportingLabels() {
    }
}
