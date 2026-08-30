package br.edu.lms.module.organization.interfaces.rest;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class ChangeMemberRoleResourceIT {

    static final String OWNER_ID   = "c1111111-1111-1111-1111-111111111111";
    static final String MEMBER_ID  = "c2222222-2222-2222-2222-222222222222";
    static final String OUTSIDER   = "c3333333-3333-3333-3333-333333333333";
    static final String ORG_ID     = "c9999999-9999-9999-9999-999999999999";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    private void insertUser(String id, String name, String email) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, email)
                .setParameter(4, "$2a$04$0000000000000000000000000000000000000000000000000000")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    @BeforeEach
    void seed() throws Exception {
        tx.begin();
        insertUser(OWNER_ID, "Role IT Owner", "role-it-owner@test.com");
        insertUser(MEMBER_ID, "Role IT Member", "role-it-member@test.com");
        insertUser(OUTSIDER, "Role IT Outsider", "role-it-outsider@test.com");
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Role IT Org").setParameter(3, OWNER_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(),?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, OWNER_ID).setParameter(3, "ADMIN_ORG").executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(),?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, MEMBER_ID).setParameter(3, "ALUNO").executeUpdate();
        tx.commit();
    }

    @AfterEach
    void cleanup() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)")
                .setParameter(1, OWNER_ID).setParameter(2, MEMBER_ID).setParameter(3, OUTSIDER).executeUpdate();
        tx.commit();
    }

    private String roleOf(String userId) {
        return em.createQuery(
                        "SELECT m.role FROM OrganizationMemberJpaEntity m WHERE m.organizationId = :o AND m.userId = :u",
                        String.class)
                .setParameter("o", ORG_ID).setParameter("u", userId)
                .getSingleResult();
    }

    private io.restassured.specification.RequestSpecification patch(String role) {
        return given().contentType("application/json").body("{\"role\":\"" + role + "\"}");
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = OWNER_ID), @Claim(key = "org", value = ORG_ID) })
    void changeRole_asAdmin_updatesTheMemberRole() {
        patch("PROFESSOR")
                .when().patch("/organizations/" + ORG_ID + "/members/" + MEMBER_ID)
                .then().statusCode(204);

        assertThat(roleOf(MEMBER_ID)).isEqualTo("PROFESSOR");
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = OWNER_ID), @Claim(key = "org", value = ORG_ID) })
    void changeRole_ofTheOwner_returns403AndKeepsTheRole() {
        patch("ALUNO")
                .when().patch("/organizations/" + ORG_ID + "/members/" + OWNER_ID)
                .then().statusCode(403)
                .body("error", equalTo("CANNOT_CHANGE_OWNER_ROLE"));

        assertThat(roleOf(OWNER_ID)).isEqualTo("ADMIN_ORG");
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = OWNER_ID), @Claim(key = "org", value = ORG_ID) })
    void changeRole_toAdminOrg_returns422() {
        patch("ADMIN_ORG")
                .when().patch("/organizations/" + ORG_ID + "/members/" + MEMBER_ID)
                .then().statusCode(422)
                .body("error", equalTo("ROLE_NOT_ASSIGNABLE"));

        assertThat(roleOf(MEMBER_ID)).isEqualTo("ALUNO");
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = OWNER_ID), @Claim(key = "org", value = ORG_ID) })
    void changeRole_ofSomeoneOutsideTheOrganization_returns404() {
        patch("GESTOR")
                .when().patch("/organizations/" + ORG_ID + "/members/" + OUTSIDER)
                .then().statusCode(404)
                .body("error", equalTo("MEMBER_NOT_FOUND"));
    }

    @Test
    @TestSecurity(user = MEMBER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = MEMBER_ID), @Claim(key = "org", value = ORG_ID) })
    void changeRole_asNonAdmin_returns403() {
        patch("PROFESSOR")
                .when().patch("/organizations/" + ORG_ID + "/members/" + MEMBER_ID)
                .then().statusCode(403);

        assertThat(roleOf(MEMBER_ID)).isEqualTo("ALUNO");
    }
}
