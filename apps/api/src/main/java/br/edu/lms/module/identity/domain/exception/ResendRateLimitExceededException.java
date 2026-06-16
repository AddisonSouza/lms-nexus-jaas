package br.edu.lms.module.identity.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class ResendRateLimitExceededException extends RuntimeException implements HttpMappable {
    public ResendRateLimitExceededException() {
        super("Resend rate limit exceeded — try again in 1 hour");
    }

    @Override public int httpStatus() { return 429; }
    @Override public String errorCode() { return "RESEND_RATE_LIMIT_EXCEEDED"; }
}
