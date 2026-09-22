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
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ListOrganizationMembersResourceIT {

    static final String OWNER_ID   = "b1111111-1111-1111-1111-111111111111";
    static final String TEACHER_ID = "b2222222-2222-2222-2222-222222222222";
    static final String GONE_ID    = "b3333333-3333-3333-3333-333333333333";
    static final String MANAGER_ID = "b4444444-4444-4444-4444-444444444444";
    static final String ORG_ID     = "b9999999-9999-9999-9999-999999999999";
    static final String OTHER_ORG  = "b8888888-8888-8888-8888-888888888888";

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
        insertUser(OWNER_ID, "Zelia Owner", "members-it-owner@test.com");
        insertUser(TEACHER_ID, "Ana Professora", "members-it-teacher@test.com");
        insertUser(GONE_ID, "Removido Silva", "members-it-gone@test.com");
        insertUser(MANAGER_ID, "Bruno Gestor", "members-it-manager@test.com");
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Members IT Org").setParameter(3, OWNER_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, OTHER_ORG).setParameter(2, "Members IT Other Org").setParameter(3, OWNER_ID)
                .executeUpdate();
        addMember(ORG_ID, OWNER_ID, "ADMIN_ORG", false);
        addMember(ORG_ID, TEACHER_ID, "PROFESSOR", false);
        addMember(ORG_ID, GONE_ID, "ALUNO", true);
        addMember(ORG_ID, MANAGER_ID, "GESTOR", false);
        tx.commit();
    }

    @AfterEach
    void cleanup() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id IN (?,?)")
                .setParameter(1, ORG_ID).setParameter(2, OTHER_ORG).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)")
                .setParameter(1, ORG_ID).setParameter(2, OTHER_ORG).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?,?)")
                .setParameter(1, OWNER_ID).setParameter(2, TEACHER_ID).setParameter(3, GONE_ID)
                .setParameter(4, MANAGER_ID).executeUpdate();
        tx.commit();
    }

    private void addMember(String orgId, String userId, String role, boolean deleted) {
        em.createNativeQuery(
                        "INSERT INTO organization_members (id, organization_id, user_id, role, joined_at, deleted_at) " +
                        "VALUES (UUID(), ?, ?, ?, NOW(6), " + (deleted ? "NOW(6)" : "NULL") + ")")
                .setParameter(1, orgId).setParameter(2, userId).setParameter(3, role)
                .executeUpdate();
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = OWNER_ID),
            @Claim(key = "org", value = ORG_ID) })
    void listMembers_asAdmin_returnsActiveMembersSortedByNameWithOwnerFlag() {
        given()
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(3))
                .body("totalElements", equalTo(3))
                .body("totalPages", equalTo(1))
                .body("number", equalTo(0))
                .body("size", equalTo(20))
                .body("content[0].name", equalTo("Ana Professora"))
                .body("content[0].email", equalTo("members-it-teacher@test.com"))
                .body("content[0].userId", equalTo(TEACHER_ID))
                .body("content[0].role", equalTo("PROFESSOR"))
                .body("content[0].owner", equalTo(false))
                .body("content[0].joinedAt", notNullValue())
                .body("content[1].name", equalTo("Bruno Gestor"))
                .body("content[1].role", equalTo("GESTOR"))
                .body("content[2].name", equalTo("Zelia Owner"))
                .body("content[2].role", equalTo("ADMIN_ORG"))
                .body("content[2].owner", equalTo(true));
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = OWNER_ID),
            @Claim(key = "org", value = ORG_ID) })
    void listMembers_searchingByName_returnsOnlyTheMatches() {
        // "ana" sozinho casaria também com members-it-m<b>ana</b>ger@test.com pelo e-mail;
        // o termo aqui é inequívoco e ainda prova que a busca ignora maiúsculas.
        given()
                .queryParam("search", "ANA PRO")
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("totalElements", equalTo(1))
                .body("content[0].userId", equalTo(TEACHER_ID));
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = OWNER_ID),
            @Claim(key = "org", value = ORG_ID) })
    void listMembers_searchingByEmail_returnsOnlyTheMatches() {
        given()
                .queryParam("search", "members-it-manager@")
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].userId", equalTo(MANAGER_ID));
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = OWNER_ID),
            @Claim(key = "org", value = ORG_ID) })
    void listMembers_searchingSomethingAbsent_returnsAnEmptyPage() {
        given()
                .queryParam("search", "ninguem-com-esse-nome")
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(0))
                .body("totalElements", equalTo(0))
                .body("totalPages", equalTo(0));
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = OWNER_ID),
            @Claim(key = "org", value = ORG_ID) })
    void listMembers_paginating_splitsTheResultAndKeepsTheTotals() {
        given()
                .queryParam("page", 0).queryParam("size", 2)
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(200)
                .body("content.name", contains("Ana Professora", "Bruno Gestor"))
                .body("totalElements", equalTo(3))
                .body("totalPages", equalTo(2))
                .body("number", equalTo(0))
                .body("size", equalTo(2));

        given()
                .queryParam("page", 1).queryParam("size", 2)
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(200)
                .body("content.name", contains("Zelia Owner"))
                .body("number", equalTo(1));
    }

    @Test
    @TestSecurity(user = MANAGER_ID, roles = {"GESTOR"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = MANAGER_ID),
            @Claim(key = "org", value = ORG_ID) })
    void listMembers_asManager_returnsActiveMembers() {
        given()
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(3))
                .body("content.name", contains("Ana Professora", "Bruno Gestor", "Zelia Owner"));
    }

    @Test
    @TestSecurity(user = MANAGER_ID, roles = {"GESTOR"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = MANAGER_ID),
            @Claim(key = "org", value = OTHER_ORG) })
    void listMembers_managerOfAnotherOrganization_returns403() {
        given()
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = TEACHER_ID),
            @Claim(key = "org", value = ORG_ID) })
    void listMembers_asNonAdmin_returns403() {
        given()
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = OWNER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = {
            @Claim(key = "sub", value = OWNER_ID),
            @Claim(key = "org", value = OTHER_ORG) })
    void listMembers_adminOfAnotherOrganization_returns403() {
        given()
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(403);
    }

    @Test
    void listMembers_unauthenticated_returns401() {
        given()
                .when().get("/organizations/" + ORG_ID + "/members")
                .then()
                .statusCode(401);
    }
}
