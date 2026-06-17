package br.edu.lms.module.identity.interfaces.rest;

import br.edu.lms.module.identity.infrastructure.security.BcryptPasswordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuthResourceIT {

    static final String USER_ID = "88888888-8888-8888-8888-888888888888";
    static final String ORG_ID  = "99999999-9999-9999-9999-999999999999";
    static final String EMAIL   = "org-scoped-it@test.com";
    static final String RAW_PASSWORD = "Password123!";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject BcryptPasswordService passwordHasher;

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organization_members WHERE user_id = ?").setParameter(1, USER_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, USER_ID).executeUpdate();
        tx.commit();
    }

    private JsonNode decodeClaims(String jwt) throws Exception {
        var payload = jwt.split("\\.")[1];
        var json = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
        return new ObjectMapper().readTree(json);
    }

    private void seedActiveUser() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, USER_ID)
                .setParameter(2, "Org Scoped IT User")
                .setParameter(3, EMAIL)
                .setParameter(4, passwordHasher.hash(RAW_PASSWORD))
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        tx.commit();
    }

    private void seedSoleOrganizationMembership(String role) throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Org Scoped IT Org").setParameter(3, USER_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, USER_ID).setParameter(3, role)
                .executeUpdate();
        tx.commit();
    }

    @Test
    void login_invalidCredentials_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"nonexistent@test.com","password":"wrong"}
                        """)
                .when().post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void refresh_withoutCookie_returns401() {
        given()
                .when().post("/auth/refresh")
                .then()
                .statusCode(401);
    }

    @Test
    void logout_withoutToken_returns401() {
        given()
                .when().post("/auth/logout")
                .then()
                .statusCode(401);
    }

    @Test
    void login_userWithoutOrganization_returnsTokenWithoutOrgClaims() throws Exception {
        seedActiveUser();

        var accessToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + RAW_PASSWORD + "\"}")
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken").toString();

        var claims = decodeClaims(accessToken);
        assertThat(claims.has("org") && !claims.get("org").isNull()).isFalse();
    }

    @Test
    void login_userWithExactlyOneOrganization_returnsTokenWithOrgClaims() throws Exception {
        seedActiveUser();
        seedSoleOrganizationMembership("ADMIN_ORG");

        var accessToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + RAW_PASSWORD + "\"}")
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken").toString();

        var claims = decodeClaims(accessToken);
        assertThat(claims.get("org").asText()).isEqualTo(ORG_ID);
        assertThat(claims.get("groups").get(0).asText()).isEqualTo("ADMIN_ORG");
    }

    @Test
    void refresh_withoutOrganizationId_userWithExactlyOneOrganization_returnsTokenWithOrgClaims() throws Exception {
        seedActiveUser();
        seedSoleOrganizationMembership("PROFESSOR");

        var loginResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + RAW_PASSWORD + "\"}")
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().response();

        var refreshCookie = loginResponse.getCookie("__refresh_token");

        var accessToken = given()
                .contentType(ContentType.JSON)
                .cookie("__refresh_token", refreshCookie)
                .body("{}")
                .when().post("/auth/refresh")
                .then().statusCode(200)
                .extract().path("accessToken").toString();

        var claims = decodeClaims(accessToken);
        assertThat(claims.get("org").asText()).isEqualTo(ORG_ID);
        assertThat(claims.get("groups").get(0).asText()).isEqualTo("PROFESSOR");
    }
}
