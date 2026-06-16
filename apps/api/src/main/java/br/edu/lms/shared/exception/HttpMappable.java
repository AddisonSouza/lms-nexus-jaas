package br.edu.lms.shared.exception;

public interface HttpMappable {
    int httpStatus();
    String errorCode();
}
