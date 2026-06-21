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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CreateOrganizationResourceIT {

    static final String OWNER_ID = "test-user-id";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    @BeforeEach
    void seedOwner() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, OWNER_ID)
                .setParameter(2, "Create Org IT Owner")
                .setParameter(3, OWNER_ID + "@it.test")
                .setParameter(4, "$2a$04$0000000000000000000000000000000000000000000000000000")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void cleanup() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organization_members WHERE user_id = ?").setParameter(1, OWNER_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE owner_id = ?").setParameter(1, OWNER_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, OWNER_ID).executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = "test-user-id", roles = {})
    @JwtSecurity(claims = { @Claim(key = "sub", value = "test-user-id") })
    void createOrganization_validRequest_returns201() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name": "Escola IT Test", "description": "Test org"}
                        """)
                .when().post("/organizations")
                .then()
                .statusCode(201)
                .body("name", equalTo("Escola IT Test"))
                .body("id", notNullValue());
    }

    @Test
    @TestSecurity(user = "test-user-id", roles = {})
    @JwtSecurity(claims = { @Claim(key = "sub", value = "test-user-id") })
    void createOrganization_duplicateName_returns409() {
        var body = """
                {"name": "Escola Dup", "description": null}
                """;
        given().contentType(ContentType.JSON).body(body).when().post("/organizations").then().statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/organizations")
                .then()
                .statusCode(409)
                .body("error", equalTo("ORGANIZATION_NAME_ALREADY_EXISTS"));
    }

    @Test
    void createOrganization_unauthenticated_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name": "Test Org"}
                        """)
                .when().post("/organizations")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "test-user-id", roles = {})
    @JwtSecurity(claims = { @Claim(key = "sub", value = "test-user-id") })
    void createOrganization_blankName_returns422() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name": ""}
                        """)
                .when().post("/organizations")
                .then()
                .statusCode(422);
    }
}
