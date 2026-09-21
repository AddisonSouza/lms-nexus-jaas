package br.edu.lms.module.organization.interfaces.rest;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * O convite nasce com {@code createdAt} e {@code expiresAt} derivados do mesmo
 * instante, a sete dias de distância. Com a JVM fora de UTC, o {@code @PrePersist}
 * da entidade sobrescrevia o {@code createdAt} pela hora do relógio local e o
 * mapper relia aquele valor como se fosse UTC: um convite enviado às 00:32
 * aparecia na lista como "Enviado em" três horas antes, no dia anterior.
 * <p>
 * O fuso fica preso em {@code America/Sao_Paulo} (UTC-3) para que a regressão
 * apareça — em UTC o defeito é invisível.
 */
@QuarkusTest
class InvitationCreatedAtTimeZoneIT {

    static final String ADMIN_ID = "7c000000-1111-1111-1111-111111111111";
    static final String ORG_ID   = "7c000000-9999-9999-9999-999999999999";
    static final String INVITEE  = "fuso@test.com";

    static TimeZone originalZone;

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject MockMailbox mailbox;

    @BeforeAll
    static void forceNonUtcZone() {
        originalZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    @AfterAll
    static void restoreZone() {
        TimeZone.setDefault(originalZone);
    }

    @BeforeEach
    void seed() throws Exception {
        mailbox.clear();
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, ADMIN_ID)
                .setParameter(2, "Timezone IT Admin")
                .setParameter(3, "timezone-it-admin@test.com")
                .setParameter(4, "$2a$04$0000000000000000000000000000000000000000000000000000")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID)
                .setParameter(2, "Timezone IT Org")
                .setParameter(3, ADMIN_ID)
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void cleanup() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM invitations WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, ADMIN_ID).executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID)})
    void invite_withJvmOutsideUtc_keepsCreatedAtAtTheRealInstant() {
        var before = Instant.now();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","role":"PROFESSOR"}
                        """.formatted(INVITEE))
                .when().post("/organizations/{id}/invitations", ORG_ID)
                .then().statusCode(201);

        var after = Instant.now();

        var json = given()
                .when().get("/organizations/{id}/invitations", ORG_ID)
                .then().statusCode(200)
                .extract().jsonPath();

        var createdAt = Instant.parse(json.getString("[0].createdAt"));
        var expiresAt = Instant.parse(json.getString("[0].expiresAt"));

        assertThat(json.getString("[0].email")).isEqualTo(INVITEE);

        // O instante real do envio, não a hora local rotulada como UTC. A folga de
        // um segundo cobre o arredondamento do DATETIME(6); o defeito deslocava a
        // data pelo offset inteiro do fuso (três horas).
        assertThat(createdAt).isBetween(before.minusSeconds(1), after.plusSeconds(1));

        // `createdAt` e `expiresAt` saem de duas chamadas a `Instant.now()`
        // consecutivas, então a distância é de sete dias a menos de microssegundos.
        assertThat(Duration.between(createdAt, expiresAt))
                .isCloseTo(Duration.ofDays(7), Duration.ofMinutes(1));
    }
}
