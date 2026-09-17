package br.edu.lms.module.curriculum.interfaces;

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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/**
 * O aluno enxerga as disciplinas das turmas de que é membro — e só elas. O
 * professor continua vendo todas as da organização.
 */
@QuarkusTest
class StudentSubjectAccessIT {

    static final String ORG_ID = "56000000-5600-5600-5600-560000000001";
    static final String TEACHER_ID = "56000000-5600-5600-5600-560000000002";
    static final String STUDENT_ID = "56000000-5600-5600-5600-560000000003";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    String classroomDoAluno;
    String classroomDeOutros;
    String subjectDoAluno;
    String subjectDeOutraTurma;
    String subjectSemTurma;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(TEACHER_ID, "Professor Access IT");
        insertUser(STUDENT_ID, "Aluno Access IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Student Access Test Org").setParameter(3, TEACHER_ID)
                .executeUpdate();
        insertOrgMember(TEACHER_ID, "PROFESSOR");
        insertOrgMember(STUDENT_ID, "ALUNO");

        classroomDoAluno = insertClassroom("Turma do aluno", "SAI001");
        classroomDeOutros = insertClassroom("Turma de outros", "SAI002");
        insertClassroomMember(classroomDoAluno, STUDENT_ID);

        subjectDoAluno = insertSubject("Disciplina da turma do aluno");
        subjectDeOutraTurma = insertSubject("Disciplina de outra turma");
        subjectSemTurma = insertSubject("Disciplina sem turma");

        linkSubjectToClassroom(subjectDoAluno, classroomDoAluno);
        linkSubjectToClassroom(subjectDeOutraTurma, classroomDeOutros);
        tx.commit();
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private void insertOrgMember(String userId, String role) {
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, ORG_ID)
                .setParameter(3, userId).setParameter(4, role)
                .executeUpdate();
    }

    private String insertClassroom(String name, String inviteCode) {
        String id = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO classrooms (id, organization_id, name, academic_period, status, invite_code, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, id).setParameter(2, ORG_ID).setParameter(3, name)
                .setParameter(4, "2026/2").setParameter(5, "ACTIVE").setParameter(6, inviteCode)
                .executeUpdate();
        return id;
    }

    private void insertClassroomMember(String classroomId, String userId) {
        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId)
                .setParameter(3, userId).setParameter(4, ORG_ID).setParameter(5, "ALUNO")
                .executeUpdate();
    }

    private String insertSubject(String name) {
        String id = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, id).setParameter(2, ORG_ID).setParameter(3, name)
                .executeUpdate();
        return id;
    }

    private void linkSubjectToClassroom(String subjectId, String classroomId) {
        em.createNativeQuery("INSERT INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectId).setParameter(2, classroomId)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM subject_classrooms WHERE subject_id IN (?,?,?)")
                .setParameter(1, subjectDoAluno).setParameter(2, subjectDeOutraTurma).setParameter(3, subjectSemTurma)
                .executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, STUDENT_ID).executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void studentListsOnlyTheSubjectsOfTheirOwnClassrooms() {
        given()
                .when().get("/subjects")
                .then()
                .statusCode(200)
                .body("id", hasItem(subjectDoAluno))
                .body("id", not(hasItem(subjectDeOutraTurma)))
                .body("id", not(hasItem(subjectSemTurma)));
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void teacherStillListsEverySubjectOfTheOrganization() {
        given()
                .when().get("/subjects")
                .then()
                .statusCode(200)
                .body("id", hasItem(subjectDoAluno))
                .body("id", hasItem(subjectDeOutraTurma))
                .body("id", hasItem(subjectSemTurma));
    }

    @Test
    void listingWithoutATokenIsStillUnauthorized() {
        given().when().get("/subjects").then().statusCode(401);
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void studentReachesTheSubjectOfTheirClassroom() {
        given()
                .when().get("/subjects/{id}", subjectDoAluno)
                .then()
                .statusCode(200)
                .body("id", equalTo(subjectDoAluno));
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void studentIsRefusedOnASubjectOfAnotherClassroom() {
        given()
                .when().get("/subjects/{id}", subjectDeOutraTurma)
                .then()
                .statusCode(403)
                .body("error", equalTo("CONTENT_ACCESS_DENIED"));
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void teacherReachesASubjectWithNoClassroomLinked() {
        given()
                .when().get("/subjects/{id}", subjectSemTurma)
                .then()
                .statusCode(200)
                .body("id", equalTo(subjectSemTurma));
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void studentIsRefusedOnTheTopicsOfASubjectTheyDoNotAttend() {
        given()
                .when().get("/subjects/{id}/topics", subjectDeOutraTurma)
                .then()
                .statusCode(403)
                .body("error", equalTo("CONTENT_ACCESS_DENIED"));
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void studentReadsTheTopicsOfTheirOwnSubject() {
        given()
                .when().get("/subjects/{id}/topics", subjectDoAluno)
                .then()
                .statusCode(200);
    }
}
