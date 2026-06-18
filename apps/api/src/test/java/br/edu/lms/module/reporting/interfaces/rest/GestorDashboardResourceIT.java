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
class GestorDashboardResourceIT {

    static final String ORG_ID = "50000000-5000-5000-5000-500000000001";
    static final String OTHER_ORG_ID = "50000000-5000-5000-5000-500000000002";
    static final String GESTOR_ID = "50000000-5000-5000-5000-500000000003";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, GESTOR_ID).setParameter(2, "Gestor Dashboard IT").setParameter(3, GESTOR_ID + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Gestor Dashboard Test Org").setParameter(3, GESTOR_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, OTHER_ORG_ID).setParameter(2, "Other Org").setParameter(3, GESTOR_ID)
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)")
                .setParameter(1, ORG_ID).setParameter(2, OTHER_ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, GESTOR_ID).executeUpdate();
        tx.commit();
    }

    @Test
    void getDashboard_withoutAuth_returns401() {
        given()
                .when().get("/organizations/{id}/gestor-dashboard", ORG_ID)
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = GESTOR_ID, roles = {"GESTOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GESTOR_ID), @Claim(key = "org", value = ORG_ID)})
    void getDashboard_ownOrganization_returns200WithClassrooms() {
        given()
                .when().get("/organizations/{id}/gestor-dashboard", ORG_ID)
                .then()
                .statusCode(200)
                .body("classrooms", equalTo(java.util.List.of()));
    }

    @Test
    @TestSecurity(user = GESTOR_ID, roles = {"GESTOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GESTOR_ID), @Claim(key = "org", value = ORG_ID)})
    void getDashboard_otherOrganization_returns403() {
        given()
                .when().get("/organizations/{id}/gestor-dashboard", OTHER_ORG_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = GESTOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GESTOR_ID), @Claim(key = "org", value = ORG_ID)})
    void getDashboard_nonGestorRole_returns403() {
        given()
                .when().get("/organizations/{id}/gestor-dashboard", ORG_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = GESTOR_ID, roles = {"GESTOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GESTOR_ID), @Claim(key = "org", value = ORG_ID)})
    void exportPdf_ownOrganization_returns200WithPdfContentType() {
        given()
                .when().get("/organizations/{id}/gestor-dashboard/pdf", ORG_ID)
                .then()
                .statusCode(200)
                .header("Content-Type", equalTo("application/pdf"));
    }

    @Test
    @TestSecurity(user = GESTOR_ID, roles = {"GESTOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GESTOR_ID), @Claim(key = "org", value = ORG_ID)})
    void exportPdf_otherOrganization_returns403() {
        given()
                .when().get("/organizations/{id}/gestor-dashboard/pdf", OTHER_ORG_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = GESTOR_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = GESTOR_ID), @Claim(key = "org", value = ORG_ID)})
    void exportPdf_nonGestorRole_returns403() {
        given()
                .when().get("/organizations/{id}/gestor-dashboard/pdf", ORG_ID)
                .then().statusCode(403);
    }
}
