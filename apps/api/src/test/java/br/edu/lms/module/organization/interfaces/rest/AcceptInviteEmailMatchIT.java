package br.edu.lms.module.organization.interfaces.rest;

import br.edu.lms.module.identity.infrastructure.security.BcryptPasswordService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * O link do convite é secreto, mas não é uma credencial: quem entra é a pessoa
 * convidada, não qualquer um que tenha o token.
 */
@QuarkusTest
class AcceptInviteEmailMatchIT {

    static final String ORG_ID = "e2e2e2e2-0000-4000-8000-00000000000a";
    static final String OWNER_ID = "e2e2e2e2-0000-4000-8000-000000000001";
    static final String GUEST_ID = "e2e2e2e2-0000-4000-8000-000000000002";
    static final String INTRUDER_ID = "e2e2e2e2-0000-4000-8000-000000000003";
    static final String INVITATION_ID = "e2e2e2e2-0000-4000-8000-00000000000f";
    static final String TOKEN = "e2e2e2e2-0000-4000-8000-0000000000ff";

    static final String GUEST_EMAIL = "guest-invite-it@test.com";
    static final String INTRUDER_EMAIL = "intruder-invite-it@test.com";
    static final String RAW_PASSWORD = "Password123!";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject BcryptPasswordService passwordHasher;

    @BeforeEach
    void seed() throws Exception {
        tx.begin();
        seedUser(OWNER_ID, "Invite Owner", "owner-invite-it@test.com");
        // O convidado se cadastrou com o e-mail em outra caixa, de propósito.
        seedUser(GUEST_ID, "Invited Guest", GUEST_EMAIL.toUpperCase());
        seedUser(INTRUDER_ID, "Intruder", INTRUDER_EMAIL);
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Invite IT Org").setParameter(3, OWNER_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT INTO invitations (id, organization_id, email, role, token, status, invited_by, expires_at, created_at) "
                        + "VALUES (?,?,?,?,?,?,?, DATE_ADD(NOW(6), INTERVAL 7 DAY), NOW(6))")
                .setParameter(1, INVITATION_ID).setParameter(2, ORG_ID).setParameter(3, GUEST_EMAIL)
                .setParameter(4, "PROFESSOR").setParameter(5, TOKEN).setParameter(6, "PENDING")
                .setParameter(7, OWNER_ID)
                .executeUpdate();
        tx.commit();
    }

    private void seedUser(String id, String name, String email) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, email)
                .setParameter(4, passwordHasher.hash(RAW_PASSWORD)).setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM invitations WHERE id = ?").setParameter(1, INVITATION_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)")
                .setParameter(1, OWNER_ID).setParameter(2, GUEST_ID).setParameter(3, INTRUDER_ID)
                .executeUpdate();
        tx.commit();
    }

    private String loginAs(String email) {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + email + "\",\"password\":\"" + RAW_PASSWORD + "\"}")
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");
    }

    private io.restassured.response.Response accept(String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when().post("/invitations/" + TOKEN + "/accept")
                .then().extract().response();
    }

    private long membersOf(String userId) {
        return ((Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM organization_members WHERE organization_id = ? AND user_id = ?")
                .setParameter(1, ORG_ID).setParameter(2, userId)
                .getSingleResult()).longValue();
    }

    @Test
    void accept_byAnotherUserHoldingTheToken_returns403AndCreatesNoMembership() {
        var response = accept(loginAs(INTRUDER_EMAIL));

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.jsonPath().getString("error")).isEqualTo("INVITATION_NOT_FOR_THIS_USER");
        assertThat(membersOf(INTRUDER_ID)).isZero();
    }

    @Test
    void accept_byTheInvitedUser_returns204AndGrantsTheInvitedRole() {
        var response = accept(loginAs(GUEST_EMAIL));

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(membersOf(GUEST_ID)).isEqualTo(1);
        var role = (String) em.createNativeQuery(
                        "SELECT role FROM organization_members WHERE organization_id = ? AND user_id = ?")
                .setParameter(1, ORG_ID).setParameter(2, GUEST_ID)
                .getSingleResult();
        assertThat(role).isEqualTo("PROFESSOR");
    }
}
