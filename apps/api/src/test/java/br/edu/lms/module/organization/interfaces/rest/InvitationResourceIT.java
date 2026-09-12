package br.edu.lms.module.organization.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class InvitationResourceIT {

    static final String USER_ID = "11111111-1111-1111-1111-111111111111";
    static final String ACCEPTEE_ID = "44444444-4444-4444-4444-444444444444";
    static final String ORG_ID  = "22222222-2222-2222-2222-222222222222";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, USER_ID)
                .setParameter(2, "Admin IT User")
                .setParameter(3, "admin-it@test.com")
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID)
                .setParameter(2, "IT Test Org")
                .setParameter(3, USER_ID)
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM invitations WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)").setParameter(1, USER_ID).setParameter(2, "33333333-3333-3333-3333-333333333333").setParameter(3, "44444444-4444-4444-4444-444444444444").executeUpdate();
        tx.commit();
    }

    // --- POST /organizations/{id}/invitations ---

    @Test
    void invite_withoutAuth_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"new@test.com","role":"PROFESSOR"}
                        """)
                .when().post("/organizations/{id}/invitations", ORG_ID)
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void invite_withoutAdminRole_returns403() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"new@test.com","role":"PROFESSOR"}
                        """)
                .when().post("/organizations/{id}/invitations", ORG_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = "wrong-org-id")})
    void invite_withWrongOrgClaim_returns403() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"new@test.com","role":"PROFESSOR"}
                        """)
                .when().post("/organizations/{id}/invitations", ORG_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void invite_validRequest_returns201() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"invited@test.com","role":"PROFESSOR"}
                        """)
                .when().post("/organizations/{id}/invitations", ORG_ID)
                .then().statusCode(201);
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void invite_emailAlreadyActiveMember_returns409() throws Exception {
        // Pre-register user as active member
        var memberId = "member-it-001";
        var membUserId = "33333333-3333-3333-3333-333333333333";
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, membUserId)
                .setParameter(2, "Existing Member")
                .setParameter(3, "already-member@test.com")
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, memberId)
                .setParameter(2, ORG_ID)
                .setParameter(3, membUserId)
                .setParameter(4, "PROFESSOR")
                .executeUpdate();
        tx.commit();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"already-member@test.com","role":"PROFESSOR"}
                        """)
                .when().post("/organizations/{id}/invitations", ORG_ID)
                .then()
                .statusCode(409)
                .body("error", equalTo("ALREADY_A_MEMBER"));
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void invite_sameEmailAgain_cancelsThePendingInvitation() throws Exception {
        for (var email : new String[]{"reinvited@test.com", "REINVITED@test.com"}) {
            given()
                    .contentType(ContentType.JSON)
                    .body("{\"email\":\"%s\",\"role\":\"PROFESSOR\"}".formatted(email))
                    .when().post("/organizations/{id}/invitations", ORG_ID)
                    .then().statusCode(201);
        }

        tx.begin();
        @SuppressWarnings("unchecked")
        var statuses = (java.util.List<String>) em.createNativeQuery(
                        "SELECT status FROM invitations WHERE organization_id = ? AND LOWER(email) = 'reinvited@test.com' ORDER BY created_at")
                .setParameter(1, ORG_ID)
                .getResultList();
        tx.commit();

        org.assertj.core.api.Assertions.assertThat(statuses).containsExactly("CANCELLED", "PENDING");
    }

    // --- POST /invitations/{token}/accept ---

    @Test
    void accept_withoutAuth_returns401() {
        given()
                .when().post("/invitations/some-token/accept")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID)})
    void accept_tokenNotFound_returns404() {
        given()
                .when().post("/invitations/nonexistent-token/accept")
                .then()
                .statusCode(404)
                .body("error", equalTo("INVITATION_NOT_FOUND"));
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID)})
    void accept_expiredToken_returns410() throws Exception {
        var expiredToken = "expired-token-it-001";
        tx.begin();
        em.createNativeQuery("""
                INSERT INTO invitations (id, organization_id, email, role, token, status, invited_by, expires_at, created_at)
                VALUES (UUID(), ?, 'exp@test.com', 'PROFESSOR', ?, 'PENDING', ?, DATE_SUB(NOW(6), INTERVAL 1 DAY), NOW(6))
                """)
                .setParameter(1, ORG_ID)
                .setParameter(2, expiredToken)
                .setParameter(3, USER_ID)
                .executeUpdate();
        tx.commit();

        given()
                .when().post("/invitations/{token}/accept", expiredToken)
                .then()
                .statusCode(410)
                .body("error", equalTo("INVITATION_EXPIRED"));
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID)})
    void accept_alreadyUsedToken_returns409() throws Exception {
        var usedToken = "used-token-it-001";
        tx.begin();
        em.createNativeQuery("""
                INSERT INTO invitations (id, organization_id, email, role, token, status, invited_by, expires_at, created_at)
                VALUES (UUID(), ?, 'used@test.com', 'PROFESSOR', ?, 'USED', ?, DATE_ADD(NOW(6), INTERVAL 7 DAY), NOW(6))
                """)
                .setParameter(1, ORG_ID)
                .setParameter(2, usedToken)
                .setParameter(3, USER_ID)
                .executeUpdate();
        tx.commit();

        given()
                .when().post("/invitations/{token}/accept", usedToken)
                .then()
                .statusCode(409)
                .body("error", equalTo("INVITATION_ALREADY_USED"));
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID)})
    void accept_cancelledToken_returns410() throws Exception {
        var cancelledToken = "cancelled-token-it-001";
        tx.begin();
        em.createNativeQuery("""
                INSERT INTO invitations (id, organization_id, email, role, token, status, invited_by, expires_at, created_at)
                VALUES (UUID(), ?, 'cancelled@test.com', 'PROFESSOR', ?, 'CANCELLED', ?, DATE_ADD(NOW(6), INTERVAL 7 DAY), NOW(6))
                """)
                .setParameter(1, ORG_ID)
                .setParameter(2, cancelledToken)
                .setParameter(3, USER_ID)
                .executeUpdate();
        tx.commit();

        given()
                .when().post("/invitations/{token}/accept", cancelledToken)
                .then()
                .statusCode(410)
                .body("error", equalTo("INVITATION_CANCELLED"));
    }

    // Remover é soft delete e uq_member (org, user) ignora o deleted_at: aceitar
    // inseria outra linha e respondia 500 (#204).
    @Test
    @TestSecurity(user = ACCEPTEE_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ACCEPTEE_ID)})
    void accept_byARemovedMember_reactivatesTheMembership() throws Exception {
        var token = "returning-member-token-it-001";
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, ACCEPTEE_ID)
                .setParameter(2, "Acceptee")
                .setParameter(3, "acceptee@test.com")
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("""
                INSERT INTO organization_members (id, organization_id, user_id, role, joined_at, deleted_at)
                VALUES ('removed-member-it-001', ?, ?, 'ALUNO', DATE_SUB(NOW(6), INTERVAL 30 DAY), DATE_SUB(NOW(6), INTERVAL 1 DAY))
                """)
                .setParameter(1, ORG_ID)
                .setParameter(2, ACCEPTEE_ID)
                .executeUpdate();
        em.createNativeQuery("""
                INSERT INTO invitations (id, organization_id, email, role, token, status, invited_by, expires_at, created_at)
                VALUES (UUID(), ?, 'acceptee@test.com', 'PROFESSOR', ?, 'PENDING', ?, DATE_ADD(NOW(6), INTERVAL 7 DAY), NOW(6))
                """)
                .setParameter(1, ORG_ID)
                .setParameter(2, token)
                .setParameter(3, USER_ID)
                .executeUpdate();
        tx.commit();

        given()
                .when().post("/invitations/{token}/accept", token)
                .then().statusCode(204);

        tx.begin();
        // joined_at sai do LocalDateTime.now() da JVM (horário local) e o NOW() do MySQL
        // pode estar em UTC: a folga de um dia absorve o fuso e ainda prova que a data
        // deixou os 30 dias semeados.
        var rows = em.createNativeQuery("""
                SELECT id, role, deleted_at IS NULL, joined_at > DATE_SUB(NOW(6), INTERVAL 1 DAY)
                FROM organization_members WHERE organization_id = ? AND user_id = ?
                """)
                .setParameter(1, ORG_ID)
                .setParameter(2, ACCEPTEE_ID)
                .getResultList();
        tx.commit();

        org.assertj.core.api.Assertions.assertThat(rows).hasSize(1);
        var row = (Object[]) rows.get(0);
        org.assertj.core.api.Assertions.assertThat(row[0]).isEqualTo("removed-member-it-001");
        org.assertj.core.api.Assertions.assertThat(row[1]).isEqualTo("PROFESSOR");
        org.assertj.core.api.Assertions.assertThat(((Number) row[2]).intValue()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(((Number) row[3]).intValue()).isEqualTo(1);
    }

    // O convite vale para o e-mail a que foi endereçado (#138), então quem aceita
    // é o convidado — autenticar como o convidante daria 403.
    @Test
    @TestSecurity(user = ACCEPTEE_ID, roles = {})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ACCEPTEE_ID)})
    void accept_validToken_returns204() throws Exception {
        var acceptToken = "valid-accept-token-it-001";
        var accepteeId = ACCEPTEE_ID;
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, accepteeId)
                .setParameter(2, "Acceptee")
                .setParameter(3, "acceptee@test.com")
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("""
                INSERT INTO invitations (id, organization_id, email, role, token, status, invited_by, expires_at, created_at)
                VALUES (UUID(), ?, 'acceptee@test.com', 'PROFESSOR', ?, 'PENDING', ?, DATE_ADD(NOW(6), INTERVAL 7 DAY), NOW(6))
                """)
                .setParameter(1, ORG_ID)
                .setParameter(2, acceptToken)
                .setParameter(3, USER_ID)
                .executeUpdate();
        tx.commit();

        // Accept as the acceptee user
        given()
                .when().post("/invitations/{token}/accept", acceptToken)
                .then().statusCode(204);
    }
}
