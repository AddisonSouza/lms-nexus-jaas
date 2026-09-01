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
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ListPendingInvitationsResourceIT {

    static final String GUEST_ID   = "d1111111-1111-1111-1111-111111111111";
    static final String INVITER_ID = "d2222222-2222-2222-2222-222222222222";
    static final String STRANGER   = "d3333333-3333-3333-3333-333333333333";
    static final String ORG_ID     = "d9999999-9999-9999-9999-999999999999";

    static final String GUEST_EMAIL = "pending-it-guest@test.com";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    private void insertUser(String id, String name, String email) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, email)
                .setParameter(4, "$2a$04$0000000000000000000000000000000000000000000000000000")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    /** @param email endereçado assim de propósito, para exercitar a comparação sem caixa. */
    private void insertInvitation(String token, String email, String status, int daysToExpiry) {
        em.createNativeQuery(
                        "INSERT INTO invitations (id, organization_id, email, role, token, status, invited_by, expires_at, created_at) " +
                        "VALUES (UUID(), ?, ?, 'PROFESSOR', ?, ?, ?, DATE_ADD(NOW(6), INTERVAL ? DAY), NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, email).setParameter(3, token)
                .setParameter(4, status).setParameter(5, INVITER_ID).setParameter(6, daysToExpiry)
                .executeUpdate();
    }

    @BeforeEach
    void seed() throws Exception {
        tx.begin();
        insertUser(GUEST_ID, "Pending IT Guest", GUEST_EMAIL);
        insertUser(INVITER_ID, "Pending IT Inviter", "pending-it-inviter@test.com");
        insertUser(STRANGER, "Pending IT Stranger", "pending-it-stranger@test.com");
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Pending IT Org").setParameter(3, INVITER_ID)
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void cleanup() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM invitations WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)")
                .setParameter(1, GUEST_ID).setParameter(2, INVITER_ID).setParameter(3, STRANGER).executeUpdate();
        tx.commit();
    }

    private void withInvitations(Runnable seedRows) throws Exception {
        tx.begin();
        seedRows.run();
        tx.commit();
    }

    @Test
    @TestSecurity(user = GUEST_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GUEST_ID)})
    void pending_returnsTheInvitationsAddressedToTheUserEmail() throws Exception {
        withInvitations(() -> insertInvitation("pending-it-tok-1", GUEST_EMAIL, "PENDING", 7));

        given()
                .when().get("/invitations/pending")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].token", equalTo("pending-it-tok-1"))
                .body("[0].organizationId", equalTo(ORG_ID))
                .body("[0].organizationName", equalTo("Pending IT Org"))
                .body("[0].role", equalTo("PROFESSOR"))
                .body("[0].expiresAt", notNullValue());
    }

    @Test
    @TestSecurity(user = GUEST_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GUEST_ID)})
    void pending_matchesTheEmailIgnoringCase() throws Exception {
        withInvitations(() -> insertInvitation("pending-it-case", GUEST_EMAIL.toUpperCase(), "PENDING", 7));

        given()
                .when().get("/invitations/pending")
                .then().statusCode(200).body("size()", equalTo(1));
    }

    @Test
    @TestSecurity(user = GUEST_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GUEST_ID)})
    void pending_ignoresUsedAndExpiredInvitations() throws Exception {
        withInvitations(() -> {
            insertInvitation("pending-it-used", GUEST_EMAIL, "USED", 7);
            insertInvitation("pending-it-expired", GUEST_EMAIL, "PENDING", -1);
        });

        given()
                .when().get("/invitations/pending")
                .then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = STRANGER, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = STRANGER)})
    void pending_doesNotLeakInvitationsAddressedToSomeoneElse() throws Exception {
        withInvitations(() -> insertInvitation("pending-it-other", GUEST_EMAIL, "PENDING", 7));

        given()
                .when().get("/invitations/pending")
                .then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    void pending_unauthenticated_returns401() {
        given()
                .when().get("/invitations/pending")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = GUEST_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GUEST_ID)})
    void pending_isNotSwallowedByTheTokenRoute() throws Exception {
        withInvitations(() -> insertInvitation("pending-it-route", GUEST_EMAIL, "PENDING", 7));

        // /invitations/{token} responderia 404 para o literal "pending".
        given()
                .when().get("/invitations/pending")
                .then().statusCode(200).body("$", instanceOf(java.util.List.class));
    }
}
