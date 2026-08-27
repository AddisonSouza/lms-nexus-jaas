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
class ListUserOrganizationsResourceIT {

    static final String USER_ID = "88888888-8888-8888-8888-888888888888";
    static final String ORG_A   = "99999999-9999-9999-9999-999999999999";
    static final String ORG_B   = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    @BeforeEach
    void seed() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, USER_ID)
                .setParameter(2, "List Orgs IT User")
                .setParameter(3, "list-orgs-it@test.com")
                .setParameter(4, "$2a$04$0000000000000000000000000000000000000000000000000000")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_A).setParameter(2, "Alfa Escola").setParameter(3, USER_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_B).setParameter(2, "Beta Escola").setParameter(3, USER_ID)
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void cleanup() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organization_members WHERE user_id = ?").setParameter(1, USER_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)").setParameter(1, ORG_A).setParameter(2, ORG_B).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, USER_ID).executeUpdate();
        tx.commit();
    }

    private void addMembership(String organizationId, String role) throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, organizationId).setParameter(2, USER_ID).setParameter(3, role)
                .executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {})
    @JwtSecurity(claims = { @Claim(key = "sub", value = USER_ID) })
    void listOrganizations_memberOfTwo_returnsBothWithRole() throws Exception {
        addMembership(ORG_A, "ADMIN_ORG");
        addMembership(ORG_B, "PROFESSOR");

        given()
                .when().get("/organizations")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].id", equalTo(ORG_A))
                .body("[0].name", equalTo("Alfa Escola"))
                .body("[0].role", equalTo("ADMIN_ORG"))
                .body("[1].id", equalTo(ORG_B))
                .body("[1].role", equalTo("PROFESSOR"));
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {})
    @JwtSecurity(claims = { @Claim(key = "sub", value = USER_ID) })
    void listOrganizations_noMembership_returnsEmptyArray() {
        given()
                .when().get("/organizations")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void listOrganizations_unauthenticated_returns401() {
        given()
                .when().get("/organizations")
                .then()
                .statusCode(401);
    }
}
