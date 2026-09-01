package br.edu.lms.module.identity.interfaces.rest;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Mudar o vínculo de um membro passa a valer na requisição seguinte, e não no
 * próximo access token.
 */
@QuarkusTest
class StaleSessionIT {

    static final String OWNER_ID  = "d1111111-1111-1111-1111-111111111111";
    static final String MEMBER_ID = "d2222222-2222-2222-2222-222222222222";
    static final String ORG_ID    = "d9999999-9999-9999-9999-999999999999";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject RedisDataSource redis;

    @ConfigProperty(name = "mp.jwt.verify.issuer") String issuer;

    private String tokenFor(String userId, String role, Instant issuedAt) {
        return Jwt.issuer(issuer)
                .subject(userId)
                .groups(Set.of(role))
                .claim("org", ORG_ID)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(Duration.ofMinutes(15)))
                .sign();
    }

    private io.restassured.specification.RequestSpecification as(String token) {
        return given().header("Authorization", "Bearer " + token);
    }

    private void insertUser(String id, String name, String email) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, email)
                .setParameter(4, "$2a$04$0000000000000000000000000000000000000000000000000000")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private void insertMember(String userId, String role) {
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(),?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, userId).setParameter(3, role)
                .executeUpdate();
    }

    @BeforeEach
    void seed() throws Exception {
        // A marca sobrevive ao teste anterior (TTL de 15 min), e um teste não
        // pode herdar a sessão obsoleta do outro.
        redis.key().del("identity:stale-since:" + OWNER_ID, "identity:stale-since:" + MEMBER_ID);

        tx.begin();
        insertUser(OWNER_ID, "Stale IT Owner", "stale-it-owner@test.com");
        insertUser(MEMBER_ID, "Stale IT Member", "stale-it-member@test.com");
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Stale IT Org").setParameter(3, OWNER_ID)
                .executeUpdate();
        insertMember(OWNER_ID, "ADMIN_ORG");
        insertMember(MEMBER_ID, "PROFESSOR");
        tx.commit();
    }

    @AfterEach
    void cleanup() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)")
                .setParameter(1, OWNER_ID).setParameter(2, MEMBER_ID).executeUpdate();
        tx.commit();
    }

    @Test
    void aRoleChangeStopsTheOldTokenOnTheNextRequest() {
        var memberToken = tokenFor(MEMBER_ID, "PROFESSOR", Instant.now().minusSeconds(60));
        var adminToken = tokenFor(OWNER_ID, "ADMIN_ORG", Instant.now());

        as(memberToken).when().get("/organizations").then().statusCode(200);

        as(adminToken).contentType("application/json").body("{\"role\":\"ALUNO\"}")
                .when().patch("/organizations/" + ORG_ID + "/members/" + MEMBER_ID)
                .then().statusCode(204);

        as(memberToken).when().get("/organizations")
                .then().statusCode(401).body("error", equalTo("SESSION_STALE"));

        // O token emitido depois da marca é aceito — é o que o front consegue
        // sozinho, renovando em silêncio.
        as(tokenFor(MEMBER_ID, "ALUNO", Instant.now()))
                .when().get("/organizations").then().statusCode(200);
    }

    @Test
    void removingAMemberStopsTheOldTokenOnTheNextRequest() {
        var memberToken = tokenFor(MEMBER_ID, "PROFESSOR", Instant.now().minusSeconds(60));
        var adminToken = tokenFor(OWNER_ID, "ADMIN_ORG", Instant.now());

        as(memberToken).when().get("/organizations").then().statusCode(200);

        as(adminToken).when().delete("/organizations/" + ORG_ID + "/members/" + MEMBER_ID)
                .then().statusCode(204);

        as(memberToken).when().get("/organizations")
                .then().statusCode(401).body("error", equalTo("SESSION_STALE"));
    }

    @Test
    void aRoleChangeLeavesEveryoneElseAlone() {
        var adminToken = tokenFor(OWNER_ID, "ADMIN_ORG", Instant.now().minusSeconds(60));

        as(adminToken).contentType("application/json").body("{\"role\":\"GESTOR\"}")
                .when().patch("/organizations/" + ORG_ID + "/members/" + MEMBER_ID)
                .then().statusCode(204);

        as(adminToken).when().get("/organizations").then().statusCode(200);
    }
}
