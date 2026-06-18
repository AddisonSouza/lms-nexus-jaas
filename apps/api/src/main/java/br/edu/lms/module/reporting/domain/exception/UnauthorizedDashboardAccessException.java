package br.edu.lms.module.reporting.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class UnauthorizedDashboardAccessException extends RuntimeException implements HttpMappable {
    public UnauthorizedDashboardAccessException() {
        super("Access denied to this dashboard");
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "DASHBOARD_ACCESS_DENIED"; }
}
