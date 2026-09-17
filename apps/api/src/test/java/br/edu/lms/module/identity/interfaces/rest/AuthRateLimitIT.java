package br.edu.lms.module.identity.interfaces.rest;

import br.edu.lms.module.identity.infrastructure.security.BcryptPasswordService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Força bruta em `/auth`: depois de N falhas o IP recebe 429 em toda a área de
 * autenticação, inclusive com a senha certa.
 */
@QuarkusTest
class AuthRateLimitIT {

    static final String USER_ID = "aa111111-1111-1111-1111-111111111111";
    static final String EMAIL = "rate-limit-it@test.com";
    static final String RAW_PASSWORD = "Password123!";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject BcryptPasswordService passwordHasher;

    @ConfigProperty(name = "lms.auth.rate-limit.max-failures") int maxFailures;

    @BeforeEach
    void seed() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, USER_ID).setParameter(2, "Rate Limit IT")
                .setParameter(3, EMAIL).setParameter(4, passwordHasher.hash(RAW_PASSWORD))
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void cleanup() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, USER_ID).executeUpdate();
        tx.commit();
    }

    private io.restassured.response.Response login(String password) {
        return given().contentType("application/json")
                .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + password + "\"}")
                .when().post("/auth/login");
    }

    private void exhaustAttempts() {
        for (int i = 0; i < maxFailures; i++) {
            login("WrongPassword" + i + "!");
        }
    }

    @Test
    void failedLoginsBelowTheLimit_stillReturn401() {
        for (int i = 0; i < maxFailures - 1; i++) {
            login("WrongPassword" + i + "!").then().statusCode(401);
        }
    }

    @Test
    void reachingTheLimit_returns429WithRetryAfter() {
        exhaustAttempts();

        login("WrongAgain1!").then()
                .statusCode(429)
                .header("Retry-After", notNullValue())
                .body("error", equalTo("AUTH_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    void whileBlocked_evenTheRightPasswordIsRefused() {
        exhaustAttempts();

        login(RAW_PASSWORD).then()
                .statusCode(429)
                .body("error", equalTo("AUTH_RATE_LIMIT_EXCEEDED"));
    }

    @Test
    void theBlockCoversTheWholeAuthArea() {
        exhaustAttempts();

        given().contentType("application/json")
                .body("{\"email\":\"" + EMAIL + "\"}")
                .when().post("/auth/forgot-password")
                .then().statusCode(429);

        given().contentType("application/json")
                .body("{\"fullName\":\"Alguem\",\"email\":\"outro-rl-it@test.com\",\"password\":\"Password123!\"}")
                .when().post("/auth/register")
                .then().statusCode(429);
    }

    @Test
    void refreshAndLogout_stayOpenForWhoeverIsAlreadySignedIn() {
        exhaustAttempts();

        // Sem cookie de refresh a resposta é 401 — o que importa é não ser 429:
        // a sessão de quem já entrou não paga pelo ataque vindo do mesmo IP.
        given().when().post("/auth/refresh").then().statusCode(not(429));
        given().when().post("/auth/logout").then().statusCode(not(429));
    }

    @Test
    void aSuccessfulLogin_clearsTheFailuresCountedSoFar() {
        for (int i = 0; i < maxFailures - 1; i++) {
            login("WrongPassword" + i + "!").then().statusCode(401);
        }

        login(RAW_PASSWORD).then().statusCode(200);

        // A contagem recomeçou: a falha seguinte volta a ser 401, não 429.
        login("WrongAgain1!").then().statusCode(401);
    }
}
