package br.edu.lms.module.organization.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class CancelInvitationResourceIT {

    static final String ADMIN_ID  = "c8c8c8c8-1111-1111-1111-111111111111";
    static final String ORG_ID    = "c8c8c8c8-9999-9999-9999-999999999999";
    static final String OTHER_ORG = "c8c8c8c8-8888-8888-8888-888888888888";
    static final String INVITATION_ID = "c8c8c8c8-7777-7777-7777-777777777777";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    private void insertInvitation(String orgId, String status, int daysToExpiry) throws Exception {
        tx.begin();
        em.createNativeQuery(
                        "INSERT INTO invitations (id, organization_id, email, role, token, status, invited_by, expires_at, created_at) " +
                        "VALUES (?, ?, 'cancel-it-guest@test.com', 'ALUNO', UUID(), ?, ?, DATE_ADD(NOW(6), INTERVAL ? DAY), NOW(6))")
                .setParameter(1, INVITATION_ID).setParameter(2, orgId).setParameter(3, status)
                .setParameter(4, ADMIN_ID).setParameter(5, daysToExpiry)
                .executeUpdate();
        tx.commit();
    }

    private String statusInDatabase() throws Exception {
        tx.begin();
        var status = (String) em.createNativeQuery("SELECT status FROM invitations WHERE id = ?")
                .setParameter(1, INVITATION_ID)
                .getSingleResult();
        tx.commit();
        return status;
    }

    @BeforeEach
    void seed() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, ADMIN_ID).setParameter(2, "Cancel IT Admin").setParameter(3, "cancel-it-admin@test.com")
                .setParameter(4, "$2a$04$0000000000000000000000000000000000000000000000000000")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        for (var org : new String[]{ORG_ID, OTHER_ORG}) {
            em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                    .setParameter(1, org).setParameter(2, "Cancel IT " + org.substring(9, 13)).setParameter(3, ADMIN_ID)
                    .executeUpdate();
        }
        tx.commit();
    }

    @AfterEach
    void cleanup() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM invitations WHERE organization_id IN (?,?)").setParameter(1, ORG_ID).setParameter(2, OTHER_ORG).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)").setParameter(1, ORG_ID).setParameter(2, OTHER_ORG).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, ADMIN_ID).executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void cancel_pendingInvitation_returns204AndCancelsIt() throws Exception {
        insertInvitation(ORG_ID, "PENDING", 7);

        given()
                .when().delete("/organizations/{id}/invitations/{invitationId}", ORG_ID, INVITATION_ID)
                .then().statusCode(204);

        assertThat(statusInDatabase()).isEqualTo("CANCELLED");
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void cancel_acceptedInvitation_returns409() throws Exception {
        insertInvitation(ORG_ID, "USED", 7);

        given()
                .when().delete("/organizations/{id}/invitations/{invitationId}", ORG_ID, INVITATION_ID)
                .then()
                .statusCode(409)
                .body("error", equalTo("INVITATION_NOT_PENDING"));

        assertThat(statusInDatabase()).isEqualTo("USED");
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void cancel_invitationOfAnotherOrganization_returns404() throws Exception {
        insertInvitation(OTHER_ORG, "PENDING", 7);

        given()
                .when().delete("/organizations/{id}/invitations/{invitationId}", ORG_ID, INVITATION_ID)
                .then()
                .statusCode(404)
                .body("error", equalTo("INVITATION_NOT_FOUND"));

        assertThat(statusInDatabase()).isEqualTo("PENDING");
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void cancel_withoutAdminRole_returns403() throws Exception {
        insertInvitation(ORG_ID, "PENDING", 7);

        given()
                .when().delete("/organizations/{id}/invitations/{invitationId}", ORG_ID, INVITATION_ID)
                .then().statusCode(403);

        assertThat(statusInDatabase()).isEqualTo("PENDING");
    }
}
