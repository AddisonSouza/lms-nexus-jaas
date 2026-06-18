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
class StudentDashboardResourceIT {

    static final String ORG_ID = "52000000-5200-5200-5200-520000000001";
    static final String STUDENT_ID = "52000000-5200-5200-5200-520000000002";
    static final String OTHER_ORG_ID = "52000000-5200-5200-5200-520000000099";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    String classroomId;
    String subjectId;
    String taskId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, STUDENT_ID).setParameter(2, "Aluno Dashboard Resource IT").setParameter(3, STUDENT_ID + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Student Dashboard Resource Test Org").setParameter(3, STUDENT_ID)
                .executeUpdate();

        classroomId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO classrooms (id, organization_id, name, academic_period, status, invite_code, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, classroomId).setParameter(2, ORG_ID).setParameter(3, "Turma Resource IT")
                .setParameter(4, "2026.1").setParameter(5, "ACTIVE").setParameter(6, "RES123")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId).setParameter(3, STUDENT_ID)
                .setParameter(4, ORG_ID).setParameter(5, "ALUNO")
                .executeUpdate();

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina Resource IT")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectId).setParameter(2, classroomId)
                .executeUpdate();

        taskId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,DATE_ADD(NOW(6), INTERVAL 3 DAY),?,NOW(6),NOW(6))")
                .setParameter(1, taskId).setParameter(2, subjectId).setParameter(3, ORG_ID).setParameter(4, STUDENT_ID)
                .setParameter(5, "Tarefa Resource IT").setParameter(6, "Descrição").setParameter(7, "PUBLISHED")
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_classrooms WHERE subject_id = ?").setParameter(1, subjectId).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, STUDENT_ID).executeUpdate();
        tx.commit();
    }

    @Test
    void getDashboard_withoutAuth_returns401() {
        given()
                .when().get("/students/me/dashboard")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID)})
    void getDashboard_studentWithPendingTask_returns200() {
        given()
                .when().get("/students/me/dashboard")
                .then()
                .statusCode(200)
                .body("pendingTasksCount", org.hamcrest.Matchers.equalTo(1))
                .body("upcomingPendingTasks[0].taskId", org.hamcrest.Matchers.equalTo(taskId));
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = OTHER_ORG_ID)})
    void getDashboard_organizationIsolation_doesNotExposeOtherOrganizationData() {
        given()
                .when().get("/students/me/dashboard")
                .then()
                .statusCode(200)
                .body("pendingTasksCount", org.hamcrest.Matchers.equalTo(0))
                .body("upcomingPendingTasks", org.hamcrest.Matchers.empty());
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID)})
    void getDashboard_nonStudentRole_returns403() {
        given()
                .when().get("/students/me/dashboard")
                .then().statusCode(403);
    }
}
