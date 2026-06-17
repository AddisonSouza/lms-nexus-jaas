package br.edu.lms.module.reporting.interfaces.rest;

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
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class AdminDashboardResourceIT {

    static final String ORG_ID = "30000000-3000-3000-3000-300000000001";
    static final String OTHER_ORG_ID = "30000000-3000-3000-3000-300000000002";
    static final String ADMIN_ID = "30000000-3000-3000-3000-300000000003";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, ADMIN_ID).setParameter(2, "Admin Dashboard IT").setParameter(3, ADMIN_ID + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Dashboard Test Org").setParameter(3, ADMIN_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, OTHER_ORG_ID).setParameter(2, "Other Org").setParameter(3, ADMIN_ID)
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)")
                .setParameter(1, ORG_ID).setParameter(2, OTHER_ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, ADMIN_ID).executeUpdate();
        tx.commit();
    }

    @Test
    void getDashboard_withoutAuth_returns401() {
        given()
                .when().get("/organizations/{id}/dashboard?from=2026-01-01&to=2026-01-31", ORG_ID)
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void getDashboard_ownOrganization_returns200WithMetrics() {
        given()
                .when().get("/organizations/{id}/dashboard?from=2026-01-01&to=2026-01-31", ORG_ID)
                .then()
                .statusCode(200)
                .body("tasksCreated", equalTo(0))
                .body("activity", equalTo(java.util.List.of()));
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void getDashboard_otherOrganization_returns403() {
        given()
                .when().get("/organizations/{id}/dashboard?from=2026-01-01&to=2026-01-31", OTHER_ORG_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void getDashboard_nonAdminRole_returns403() {
        given()
                .when().get("/organizations/{id}/dashboard?from=2026-01-01&to=2026-01-31", ORG_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void getDashboard_fromAfterTo_returns400() {
        given()
                .when().get("/organizations/{id}/dashboard?from=2026-02-01&to=2026-01-01", ORG_ID)
                .then().statusCode(400);
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void exportPdf_ownOrganization_returns200WithPdfContentType() {
        given()
                .when().get("/organizations/{id}/reports/pdf?from=2026-01-01&to=2026-01-31", ORG_ID)
                .then()
                .statusCode(200)
                .header("Content-Type", equalTo("application/pdf"));
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void exportPdf_otherOrganization_returns403() {
        given()
                .when().get("/organizations/{id}/reports/pdf?from=2026-01-01&to=2026-01-31", OTHER_ORG_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void exportPdf_nonAdminRole_returns403() {
        given()
                .when().get("/organizations/{id}/reports/pdf?from=2026-01-01&to=2026-01-31", ORG_ID)
                .then().statusCode(403);
    }
}
