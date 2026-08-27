package br.edu.lms.module.identity.interfaces.rest;

import br.edu.lms.module.identity.infrastructure.security.BcryptPasswordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A user who belongs to more than one organization has to reach the app and
 * stay where they switched to — a reload used to drop them back to /welcome.
 */
@QuarkusTest
class MultiOrganizationSessionIT {

    static final String USER_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    static final String ORG_ALFA = "cccccccc-cccc-cccc-cccc-cccccccccccc";
    static final String ORG_BETA = "dddddddd-dddd-dddd-dddd-dddddddddddd";
    static final String EMAIL = "multi-org-it@test.com";
    static final String RAW_PASSWORD = "Password123!";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject BcryptPasswordService passwordHasher;

    @BeforeEach
    void seed() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, USER_ID)
                .setParameter(2, "Multi Org IT User")
                .setParameter(3, EMAIL)
                .setParameter(4, passwordHasher.hash(RAW_PASSWORD))
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        // Beta is inserted first on purpose: the login picks by name, not by
        // insertion order.
        seedMembership(ORG_BETA, "Beta Escola", "PROFESSOR");
        seedMembership(ORG_ALFA, "Alfa Escola", "ADMIN_ORG");
        tx.commit();
    }

    private void seedMembership(String organizationId, String name, String role) {
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, organizationId).setParameter(2, name).setParameter(3, USER_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, organizationId).setParameter(2, USER_ID).setParameter(3, role)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organization_members WHERE user_id = ?").setParameter(1, USER_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)")
                .setParameter(1, ORG_ALFA).setParameter(2, ORG_BETA).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, USER_ID).executeUpdate();
        tx.commit();
    }

    private JsonNode decodeClaims(String jwt) throws Exception {
        var payload = jwt.split("\\.")[1];
        var json = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
        return new ObjectMapper().readTree(json);
    }

    private Response login() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + RAW_PASSWORD + "\"}")
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().response();
    }

    private Response refresh(String refreshCookie) {
        return given()
                .contentType(ContentType.JSON)
                .cookie("__refresh_token", refreshCookie)
                .body("{}")
                .when().post("/auth/refresh")
                .then().statusCode(200)
                .extract().response();
    }

    private Response switchTo(String refreshCookie, String organizationId) {
        return given()
                .contentType(ContentType.JSON)
                .cookie("__refresh_token", refreshCookie)
                .body("{\"organizationId\":\"" + organizationId + "\"}")
                .when().post("/auth/switch-organization")
                .then().statusCode(200)
                .extract().response();
    }

    @Test
    void login_userWithSeveralOrganizations_entersTheFirstByName() throws Exception {
        var claims = decodeClaims(login().path("accessToken").toString());

        assertThat(claims.get("org").asText()).isEqualTo(ORG_ALFA);
        assertThat(claims.get("groups").get(0).asText()).isEqualTo("ADMIN_ORG");
    }

    @Test
    void refresh_afterLogin_keepsTheOrganizationTheSessionIsIn() throws Exception {
        var loginResponse = login();

        var claims = decodeClaims(refresh(loginResponse.getCookie("__refresh_token")).path("accessToken").toString());

        assertThat(claims.get("org").asText()).isEqualTo(ORG_ALFA);
    }

    @Test
    void refresh_afterSwitching_staysInTheChosenOrganization() throws Exception {
        var loginResponse = login();
        var switchResponse = switchTo(loginResponse.getCookie("__refresh_token"), ORG_BETA);

        assertThat(decodeClaims(switchResponse.path("accessToken").toString()).get("org").asText())
                .isEqualTo(ORG_BETA);

        // The reload that used to undo the switch.
        var claims = decodeClaims(refresh(switchResponse.getCookie("__refresh_token")).path("accessToken").toString());

        assertThat(claims.get("org").asText()).isEqualTo(ORG_BETA);
        assertThat(claims.get("groups").get(0).asText()).isEqualTo("PROFESSOR");
    }

    @Test
    void refresh_afterTheMembershipIsRevoked_fallsBackToARemainingOrganization() throws Exception {
        var loginResponse = login();
        var switchResponse = switchTo(loginResponse.getCookie("__refresh_token"), ORG_BETA);

        tx.begin();
        em.createNativeQuery("UPDATE organization_members SET deleted_at = NOW(6) WHERE user_id = ? AND organization_id = ?")
                .setParameter(1, USER_ID).setParameter(2, ORG_BETA)
                .executeUpdate();
        tx.commit();

        var claims = decodeClaims(refresh(switchResponse.getCookie("__refresh_token")).path("accessToken").toString());

        assertThat(claims.get("org").asText()).isEqualTo(ORG_ALFA);
        assertThat(claims.get("groups").get(0).asText()).isEqualTo("ADMIN_ORG");
    }
}
