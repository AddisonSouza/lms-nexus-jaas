package br.edu.lms.module.classroom.interfaces;

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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * A resposta de adicionar um membro à turma precisa trazer o `joinedAt` que o
 * banco gravou. Ele saía nulo — o repositório devolvia o domínio de entrada, sem
 * o valor criado no @PrePersist —, o Zod do front recusava a resposta e o
 * diálogo ficava aberto com o membro já dentro da turma.
 */
@QuarkusTest
class AddClassroomMemberResponseIT {

    static final String ORG_ID = "55000000-5500-5500-5500-550000000001";
    static final String ADMIN_ID = "55000000-5500-5500-5500-550000000002";
    static final String STUDENT_ID = "55000000-5500-5500-5500-550000000003";
    static final String CLASSROOM_ID = "55000000-5500-5500-5500-550000000004";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(ADMIN_ID, "Admin AddMember IT");
        insertUser(STUDENT_ID, "Aluno AddMember IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Add Member Test Org").setParameter(3, ADMIN_ID)
                .executeUpdate();
        insertMember(ADMIN_ID, "ADMIN_ORG");
        insertMember(STUDENT_ID, "ALUNO");

        em.createNativeQuery("INSERT IGNORE INTO classrooms (id, name, academic_period, status, invite_code, organization_id, created_at) "
                        + "VALUES (?,?,?,?,?,?,NOW(6))")
                .setParameter(1, CLASSROOM_ID).setParameter(2, "Turma AddMember IT").setParameter(3, "2026.2")
                .setParameter(4, "ACTIVE").setParameter(5, "ADDM01").setParameter(6, ORG_ID)
                .executeUpdate();
        tx.commit();
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private void insertMember(String userId, String role) {
        em.createNativeQuery("INSERT IGNORE INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(),?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, userId).setParameter(3, role)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM notifications WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE classroom_id = ?").setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE id = ?").setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)").setParameter(1, ADMIN_ID).setParameter(2, STUDENT_ID).executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID) })
    void addMember_returnsTheMemberWithTheJoinedAtTheDatabaseGenerated() {
        given()
                .contentType("application/json")
                .body("{\"userId\":\"" + STUDENT_ID + "\",\"role\":\"ALUNO\"}")
                .when().post("/classrooms/" + CLASSROOM_ID + "/members")
                .then()
                .statusCode(201)
                .body("userId", equalTo(STUDENT_ID))
                .body("role", equalTo("ALUNO"))
                .body("classroomId", equalTo(CLASSROOM_ID))
                .body("id", notNullValue())
                .body("joinedAt", notNullValue());
    }

    @Test
    @TestSecurity(user = ADMIN_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = ADMIN_ID), @Claim(key = "org", value = ORG_ID) })
    void addedMember_showsUpInTheListWithTheSameJoinedAt() {
        var joinedAt = given()
                .contentType("application/json")
                .body("{\"userId\":\"" + STUDENT_ID + "\",\"role\":\"ALUNO\"}")
                .when().post("/classrooms/" + CLASSROOM_ID + "/members")
                .then().statusCode(201)
                .extract().path("joinedAt").toString();

        given()
                .when().get("/classrooms/" + CLASSROOM_ID + "/members")
                .then()
                .statusCode(200)
                .body("find { it.userId == '" + STUDENT_ID + "' }.joinedAt", equalTo(joinedAt));
    }
}
