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
import static org.hamcrest.Matchers.*;

/**
 * O vínculo de professor guarda o `memberId`, mas o JWT só carrega o `userId`.
 * Sem `teacherUserIds` na resposta, o frontend não tem como saber se quem está
 * olhando leciona a disciplina — listar os membros da organização é privilégio
 * de ADMIN_ORG, então a tela não podia traduzir por conta própria.
 */
@QuarkusTest
class SubjectTeacherUserIdsIT {

    static final String ORG_ID     = "57000000-5700-5700-5700-570000000001";
    static final String ADMIN_ID   = "57000000-5700-5700-5700-570000000002";
    static final String TEACHER_ID = "57000000-5700-5700-5700-570000000003";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    String teacherMemberId;
    String subjectId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(ADMIN_ID, "Admin TeacherIds IT");
        insertUser(TEACHER_ID, "Professor TeacherIds IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "TeacherIds Test Org").setParameter(3, ADMIN_ID)
                .executeUpdate();

        insertOrgMember(UUID.randomUUID().toString(), ADMIN_ID, "ADMIN_ORG");
        teacherMemberId = UUID.randomUUID().toString();
        insertOrgMember(teacherMemberId, TEACHER_ID, "PROFESSOR");

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Matemática TeacherIds")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_teachers (subject_id, member_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectId).setParameter(2, teacherMemberId)
                .executeUpdate();
        tx.commit();
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private void insertOrgMember(String memberId, String userId, String role) {
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, memberId).setParameter(2, ORG_ID)
                .setParameter(3, userId).setParameter(4, role)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM subject_teachers WHERE subject_id = ?").setParameter(1, subjectId).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)")
                .setParameter(1, ADMIN_ID).setParameter(2, TEACHER_ID).executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID) })
    void getSubject_carriesTheTeachersBothAsMemberAndAsUser() {
        given()
                .when().get("/subjects/{id}", subjectId)
                .then()
                .statusCode(200)
                .body("teacherMemberIds", contains(teacherMemberId))
                .body("teacherUserIds", contains(TEACHER_ID));
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID) })
    void listSubjects_carriesTheTeacherUserIdsToo() {
        given()
                .when().get("/subjects")
                .then()
                .statusCode(200)
                .body("find { it.id == '" + subjectId + "' }.teacherUserIds", contains(TEACHER_ID));
    }

    // Disciplina sem professor não vira lista nula, senão o Zod do front recusa
    // a resposta inteira.
    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID) })
    void createSubject_startsWithNoTeachers() {
        given()
                .contentType("application/json")
                .body("""
                        {"name":"Disciplina Recém-Criada TeacherIds"}
                        """)
                .when().post("/subjects")
                .then()
                .statusCode(201)
                .body("teacherUserIds", empty());
    }
}
