package br.edu.lms.module.organization.infrastructure.mail;

import br.edu.lms.module.organization.domain.event.MemberInvitedEvent;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class InvitationMailService {

    private final Mailer mailer;

    @ConfigProperty(name = "lms.app.base-url", defaultValue = "http://localhost:5173")
    String baseUrl;

    void onMemberInvited(@Observes MemberInvitedEvent event) {
        var acceptUrl = baseUrl + "/invitations/" + event.token() + "/accept";
        mailer.send(Mail.withHtml(
                event.email(),
                "Convite para organização — LMS Nexus",
                buildInviteEmailBody(acceptUrl)
        ));
        log.debug("Invitation email sent to {}", event.email());
    }

    private String buildInviteEmailBody(String url) {
        return """
                <html>
                <body>
                  <h2>Você foi convidado para uma organização no LMS Nexus!</h2>
                  <p>Clique no link abaixo para aceitar o convite. O link expira em 7 dias.</p>
                  <a href="%s">Aceitar convite</a>
                  <p>Se você não esperava este convite, pode ignorar este e-mail.</p>
                </body>
                </html>
                """.formatted(url);
    }
}
