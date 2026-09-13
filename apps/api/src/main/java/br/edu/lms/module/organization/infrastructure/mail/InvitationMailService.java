package br.edu.lms.module.organization.infrastructure.mail;

import br.edu.lms.module.organization.domain.event.MemberInvitedEvent;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
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

    // Layout compartilhado dos e-mails (templates/mail/layout.html).
    @Inject
    @Location("mail/invitation.html")
    Template invitationTemplate;

    void onMemberInvited(@Observes MemberInvitedEvent event) {
        var acceptUrl = baseUrl + "/invitations/" + event.token() + "/accept";
        mailer.send(Mail.withHtml(
                event.email(),
                "Convite para organização — LMS Nexus",
                invitationTemplate.data("actionUrl", acceptUrl).render()
        ));
        log.debug("Invitation email sent to {}", event.email());
    }
}
