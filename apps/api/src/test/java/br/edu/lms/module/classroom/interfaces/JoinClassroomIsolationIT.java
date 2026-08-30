package br.edu.lms.module.classroom.interfaces;

import br.edu.lms.module.identity.infrastructure.security.BcryptPasswordService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * O código de convite da turma não pode atravessar organizações: quem pertence à
 * Alfa não entra numa turma da Beta, mesmo tendo o código correto. E quem entra
 * pelo código entra como ALUNO, que nunca recebe o código de volta (RF-08).
 */
@QuarkusTest
class JoinClassroomIsolationIT {

    static final String USER_ID = "e1e1e1e1-0000-4000-8000-000000000001";
    static final String ORG_ALFA = "e1e1e1e1-0000-4000-8000-00000000000a";
    static final String ORG_BETA = "e1e1e1e1-0000-4000-8000-00000000000b";
    static final String CLASSROOM_ALFA = "e1e1e1e1-0000-4000-8000-00000000000c";
    static final String CLASSROOM_BETA = "e1e1e1e1-0000-4000-8000-00000000000d";
    static final String CODE_ALFA = "ALFA11";
    static final String CODE_BETA = "BETA22";
    static final String EMAIL = "join-isolation-it@test.com";
    static final String RAW_PASSWORD = "Password123!";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject BcryptPasswordService passwordHasher;

    @BeforeEach
    void seed() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, USER_ID)
                .setParameter(2, "Join Isolation IT User")
                .setParameter(3, EMAIL)
                .setParameter(4, passwordHasher.hash(RAW_PASSWORD))
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        // O usuário só pertence à Alfa. A Beta existe e tem turma, mas não é dele.
        seedOrganization(ORG_ALFA, "Alfa Escola");
        seedOrganization(ORG_BETA, "Beta Escola");
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_ALFA).setParameter(2, USER_ID).setParameter(3, "ALUNO")
                .executeUpdate();
        seedClassroom(CLASSROOM_ALFA, ORG_ALFA, "Turma Alfa", CODE_ALFA);
        seedClassroom(CLASSROOM_BETA, ORG_BETA, "Turma Beta", CODE_BETA);
        tx.commit();
    }

    private void seedOrganization(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, id).setParameter(2, name).setParameter(3, USER_ID)
                .executeUpdate();
    }

    private void seedClassroom(String id, String organizationId, String name, String code) {
        em.createNativeQuery("INSERT IGNORE INTO classrooms (id, name, academic_period, status, invite_code, organization_id, created_at) "
                        + "VALUES (?,?,?,?,?,?,NOW(6))")
                .setParameter(1, id).setParameter(2, name).setParameter(3, "2026.1")
                .setParameter(4, "ACTIVE").setParameter(5, code).setParameter(6, organizationId)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM classroom_members WHERE user_id = ?").setParameter(1, USER_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE id IN (?,?)")
                .setParameter(1, CLASSROOM_ALFA).setParameter(2, CLASSROOM_BETA).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE user_id = ?").setParameter(1, USER_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)")
                .setParameter(1, ORG_ALFA).setParameter(2, ORG_BETA).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, USER_ID).executeUpdate();
        tx.commit();
    }

    private String login() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + RAW_PASSWORD + "\"}")
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");
    }

    private io.restassured.response.Response join(String token, String code) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"inviteCode\":\"" + code + "\"}")
                .when().post("/classrooms/join")
                .then().extract().response();
    }

    private long membershipsInOrg(String organizationId) {
        return ((Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM classroom_members WHERE user_id = ? AND organization_id = ?")
                .setParameter(1, USER_ID).setParameter(2, organizationId)
                .getSingleResult()).longValue();
    }

    @Test
    void join_withCodeFromAnotherOrganization_returns404AndCreatesNoMembership() {
        var response = join(login(), CODE_BETA);

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("error")).isEqualTo("INVALID_INVITE_CODE");
        assertThat(membershipsInOrg(ORG_BETA)).isZero();
    }

    @Test
    void join_withCodeFromOwnOrganization_returns201WithoutLeakingTheCode() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + login())
                .body("{\"inviteCode\":\"" + CODE_ALFA + "\"}")
                .when().post("/classrooms/join")
                .then()
                .statusCode(201)
                .body("id", equalTo(CLASSROOM_ALFA))
                // Quem entra pelo código entra como ALUNO e nunca recebe o código.
                .body("inviteCode", nullValue());
    }

    @Test
    void join_whenAlreadyMember_returns200AndDoesNotDuplicateTheMembership() {
        var token = login();
        assertThat(join(token, CODE_ALFA).statusCode()).isEqualTo(201);

        var second = join(token, CODE_ALFA);

        assertThat(second.statusCode()).isEqualTo(200);
        assertThat(second.jsonPath().getString("inviteCode")).isNull();
        assertThat(membershipsInOrg(ORG_ALFA)).isEqualTo(1);
    }
}
