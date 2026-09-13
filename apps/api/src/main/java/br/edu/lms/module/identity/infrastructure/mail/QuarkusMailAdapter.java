package br.edu.lms.module.identity.infrastructure.mail;

import br.edu.lms.module.identity.domain.model.Email;
import br.edu.lms.module.identity.domain.port.out.EmailPort;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class QuarkusMailAdapter implements EmailPort {

    private final Mailer mailer;

    @ConfigProperty(name = "lms.auth.confirmation-token.ttl-hours", defaultValue = "24")
    int confirmationTtlHours;

    @ConfigProperty(name = "lms.auth.password-reset.url", defaultValue = "http://localhost:5173/reset-password")
    String passwordResetBaseUrl;

    @ConfigProperty(name = "lms.app.base-url", defaultValue = "http://localhost:5173")
    String baseUrl;

    // Layout compartilhado dos e-mails (templates/mail/layout.html).
    @Inject
    @Location("mail/confirm-email.html")
    Template confirmEmailTemplate;

    @Inject
    @Location("mail/password-reset.html")
    Template passwordResetTemplate;

    @Override
    public void sendConfirmationEmail(Email to, String token) {
        var confirmationUrl = baseUrl + "/confirm-email?token=" + token;
        mailer.send(Mail.withHtml(
                to.getValue(),
                "Confirme seu e-mail — LMS Nexus",
                confirmEmailTemplate
                        .data("actionUrl", confirmationUrl)
                        .data("ttlHours", confirmationTtlHours)
                        .render()
        ));
        log.debug("Confirmation email sent to {}", to.getValue());
    }

    @Override
    public void sendPasswordResetEmail(Email to, String token) {
        var resetUrl = passwordResetBaseUrl + "?token=" + token;
        mailer.send(Mail.withHtml(
                to.getValue(),
                "Redefinição de senha — LMS Nexus",
                passwordResetTemplate.data("actionUrl", resetUrl).render()
        ));
        log.debug("Password reset email sent to {}", to.getValue());
    }
}
