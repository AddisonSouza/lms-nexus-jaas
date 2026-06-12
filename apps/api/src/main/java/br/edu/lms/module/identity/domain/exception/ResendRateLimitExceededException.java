package br.edu.lms.module.identity.domain.exception;

public class ResendRateLimitExceededException extends RuntimeException {
    public ResendRateLimitExceededException() {
        super("Resend rate limit exceeded — try again in 1 hour");
    }
}
