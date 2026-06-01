package br.edu.lms.module.identity.infrastructure.mail;

import br.edu.lms.module.identity.domain.model.Email;
import br.edu.lms.module.identity.domain.port.out.EmailPort;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class QuarkusMailAdapter implements EmailPort {

    private final Mailer mailer;

    @ConfigProperty(name = "lms.auth.confirmation-token.ttl-hours", defaultValue = "24")
    int tokenTtlHours;

    @Override
    public void sendConfirmationEmail(Email to, String token) {
        var confirmationUrl = "http://localhost:5173/confirm-email?token=" + token;

        mailer.send(Mail.withHtml(
                to.getValue(),
                "Confirme seu e-mail — LMS Nexus",
                buildEmailBody(confirmationUrl, tokenTtlHours)
        ));

        log.debug("Confirmation email sent to {}", to.getValue());
    }

    private String buildEmailBody(String confirmationUrl, int ttlHours) {
        return """
                <html>
                <body>
                  <h2>Bem-vindo ao LMS Nexus!</h2>
                  <p>Clique no link abaixo para confirmar seu e-mail. O link expira em %d horas.</p>
                  <a href="%s">Confirmar e-mail</a>
                  <p>Se você não criou uma conta, ignore este e-mail.</p>
                </body>
                </html>
                """.formatted(ttlHours, confirmationUrl);
    }
}
