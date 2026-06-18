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

import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ProfessorDashboardResourceIT {

    static final String ORG_ID = "51000000-5100-5100-5100-510000000001";
    static final String TEACHER_ID = "51000000-5100-5100-5100-510000000002";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    String subjectId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, "Professor Dashboard IT").setParameter(3, TEACHER_ID + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Professor Dashboard Test Org").setParameter(3, TEACHER_ID)
                .executeUpdate();

        String memberId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, memberId).setParameter(2, ORG_ID).setParameter(3, TEACHER_ID).setParameter(4, "PROFESSOR")
                .executeUpdate();

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina Professor Resource IT")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_teachers (subject_id, member_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectId).setParameter(2, memberId)
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM subject_teachers WHERE subject_id = ?").setParameter(1, subjectId).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, TEACHER_ID).executeUpdate();
        tx.commit();
    }

    @Test
    void getDashboard_withoutAuth_returns401() {
        given()
                .when().get("/subjects/{id}/dashboard", subjectId)
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = TEACHER_ID)})
    void getDashboard_professorAssignedToSubject_returns200() {
        given()
                .when().get("/subjects/{id}/dashboard", subjectId)
                .then()
                .statusCode(200)
                .body("pendingEvaluationsCount", org.hamcrest.Matchers.equalTo(0));
    }

    @Test
    @TestSecurity(user = "51000000-5100-5100-5100-510000000099", roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = "51000000-5100-5100-5100-510000000099")})
    void getDashboard_professorNotAssignedToSubject_returns403() {
        given()
                .when().get("/subjects/{id}/dashboard", subjectId)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = TEACHER_ID)})
    void getDashboard_nonProfessorRole_returns403() {
        given()
                .when().get("/subjects/{id}/dashboard", subjectId)
                .then().statusCode(403);
    }
}
