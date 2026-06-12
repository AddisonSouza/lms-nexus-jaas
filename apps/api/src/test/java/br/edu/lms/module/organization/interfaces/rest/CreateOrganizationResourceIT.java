package br.edu.lms.module.organization.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CreateOrganizationResourceIT {

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
