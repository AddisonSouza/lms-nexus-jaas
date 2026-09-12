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
class ListOrganizationInvitationsResourceIT {

    static final String ADMIN_ID  = "a7a7a7a7-1111-1111-1111-111111111111";
    static final String ORG_ID    = "a7a7a7a7-9999-9999-9999-999999999999";
    static final String OTHER_ORG = "a7a7a7a7-8888-8888-8888-888888888888";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    private void insertInvitation(String orgId, String email, String status, int daysToExpiry, int daysAgo) {
        em.createNativeQuery(
                        "INSERT INTO invitations (id, organization_id, email, role, token, status, invited_by, expires_at, created_at) " +
                        "VALUES (UUID(), ?, ?, 'ALUNO', UUID(), ?, ?, DATE_ADD(NOW(6), INTERVAL ? DAY), DATE_SUB(NOW(6), INTERVAL ? DAY))")
                .setParameter(1, orgId).setParameter(2, email).setParameter(3, status)
                .setParameter(4, ADMIN_ID).setParameter(5, daysToExpiry).setParameter(6, daysAgo)
                .executeUpdate();
    }

    @BeforeEach
    void seed() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, ADMIN_ID).setParameter(2, "Invites IT Admin").setParameter(3, "invites-it-admin@test.com")
                .setParameter(4, "$2a$04$0000000000000000000000000000000000000000000000000000")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        for (var org : new String[]{ORG_ID, OTHER_ORG}) {
            em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                    .setParameter(1, org).setParameter(2, "Invites IT " + org.substring(9, 13)).setParameter(3, ADMIN_ID)
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
    void list_returnsEveryStateNewestFirstWithoutTheToken() throws Exception {
        tx.begin();
        insertInvitation(ORG_ID, "pending@test.com", "PENDING", 7, 0);
        insertInvitation(ORG_ID, "expired@test.com", "PENDING", -1, 1);
        insertInvitation(ORG_ID, "used@test.com", "USED", 7, 2);
        insertInvitation(ORG_ID, "cancelled@test.com", "CANCELLED", 7, 3);
        insertInvitation(OTHER_ORG, "elsewhere@test.com", "PENDING", 7, 0);
        tx.commit();

        given()
                .when().get("/organizations/{id}/invitations", ORG_ID)
                .then()
                .statusCode(200)
                .body("email", contains("pending@test.com", "expired@test.com", "used@test.com", "cancelled@test.com"))
                .body("status", contains("PENDING", "EXPIRED", "USED", "CANCELLED"))
                .body("[0].id", notNullValue())
                .body("[0].role", equalTo("ALUNO"))
                .body("[0].invitedByName", equalTo("Invites IT Admin"))
                .body("[0].createdAt", notNullValue())
                .body("[0].expiresAt", notNullValue())
                .body("[0]", not(hasKey("token")));
    }

    @Test
    void list_withoutAuth_returns401() {
        given()
                .when().get("/organizations/{id}/invitations", ORG_ID)
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void list_withoutAdminRole_returns403() {
        given()
                .when().get("/organizations/{id}/invitations", ORG_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = OTHER_ORG)})
    void list_ofAnotherOrganization_returns403() {
        given()
                .when().get("/organizations/{id}/invitations", ORG_ID)
                .then().statusCode(403);
    }
}
