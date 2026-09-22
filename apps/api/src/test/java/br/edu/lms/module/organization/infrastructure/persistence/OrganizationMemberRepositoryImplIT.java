package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.organization.domain.model.MemberRole;
import br.edu.lms.module.organization.domain.model.OrganizationMember;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class OrganizationMemberRepositoryImplIT {

    static final String USER_ID = "55555555-5555-5555-5555-555555555555";
    static final String ORG_1   = "66666666-6666-6666-6666-666666666666";
    static final String ORG_2   = "77777777-7777-7777-7777-777777777777";

    // Nomes propositalmente fora de ordem alfabética em relação aos ids, para que
    // a ordenação por nome não passe por acidente.
    static final String USER_A  = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";
    static final String USER_B  = "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb";
    static final String USER_C  = "cccccccc-cccc-4ccc-8ccc-cccccccccccc";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject OrganizationMemberRepositoryImpl sut;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, USER_ID)
                .setParameter(2, "Membership IT User")
                .setParameter(3, "membership-it@test.com")
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        insertUser(USER_A, "Ana Beatriz Souza", "ana.beatriz@test.com");
        insertUser(USER_B, "Bruno Carvalho", "bruno.carvalho@test.com");
        insertUser(USER_C, "Carla Dias", "carla.dias@example.org");
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_1).setParameter(2, "Org 1").setParameter(3, USER_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_2).setParameter(2, "Org 2").setParameter(3, USER_ID)
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM organization_members WHERE user_id IN (?,?,?,?)")
                .setParameter(1, USER_ID).setParameter(2, USER_A)
                .setParameter(3, USER_B).setParameter(4, USER_C)
                .executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)").setParameter(1, ORG_1).setParameter(2, ORG_2).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?,?)")
                .setParameter(1, USER_ID).setParameter(2, USER_A)
                .setParameter(3, USER_B).setParameter(4, USER_C)
                .executeUpdate();
        tx.commit();
    }

    @Test
    void findOrganizationsByUser_noMembership_returnsEmpty() {
        var result = sut.findOrganizationsByUser(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void findOrganizationsByUser_exactlyOneActiveMembership_returnsIt() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_1).setParameter(2, USER_ID).setParameter(3, "ADMIN_ORG")
                .executeUpdate();
        tx.commit();

        var result = sut.findOrganizationsByUser(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).organizationId()).isEqualTo(ORG_1);
        assertThat(result.get(0).role()).isEqualTo("ADMIN_ORG");
    }

    @Test
    void findOrganizationsByUser_multipleActiveMemberships_returnsAllOrderedByOrganizationName() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_2).setParameter(2, USER_ID).setParameter(3, "PROFESSOR")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_1).setParameter(2, USER_ID).setParameter(3, "ADMIN_ORG")
                .executeUpdate();
        tx.commit();

        var result = sut.findOrganizationsByUser(USER_ID);

        // The login picks the first of this list, so the order has to be stable
        // and the same one the sidebar switcher draws.
        assertThat(result).hasSize(2);
        assertThat(result.get(0).organizationId()).isEqualTo(ORG_1);
        assertThat(result.get(1).organizationId()).isEqualTo(ORG_2);
    }

    @Test
    void findOrganizationsByUser_softDeletedOrganization_isIgnored() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_1).setParameter(2, USER_ID).setParameter(3, "ADMIN_ORG")
                .executeUpdate();
        em.createNativeQuery("UPDATE organizations SET deleted_at = NOW(6) WHERE id = ?")
                .setParameter(1, ORG_1)
                .executeUpdate();
        tx.commit();

        assertThat(sut.findOrganizationsByUser(USER_ID)).isEmpty();
    }

    @Test
    void findOrganizationsByUser_softDeletedMembership_isIgnored() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at, deleted_at) VALUES (UUID(), ?, ?, ?, NOW(6), NOW(6))")
                .setParameter(1, ORG_1).setParameter(2, USER_ID).setParameter(3, "ADMIN_ORG")
                .executeUpdate();
        tx.commit();

        var result = sut.findOrganizationsByUser(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void findUserOrganizations_activeMemberships_returnsIdNameAndRoleOrderedByName() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_2).setParameter(2, USER_ID).setParameter(3, "PROFESSOR")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_1).setParameter(2, USER_ID).setParameter(3, "ADMIN_ORG")
                .executeUpdate();
        tx.commit();

        var result = sut.findUserOrganizations(USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(ORG_1);
        assertThat(result.get(0).getName()).isEqualTo("Org 1");
        assertThat(result.get(0).getRole()).isEqualTo(MemberRole.ADMIN_ORG);
        assertThat(result.get(1).getId()).isEqualTo(ORG_2);
        assertThat(result.get(1).getRole()).isEqualTo(MemberRole.PROFESSOR);
    }

    @Test
    void findUserOrganizations_noMembership_returnsEmpty() {
        assertThat(sut.findUserOrganizations(USER_ID)).isEmpty();
    }

    @Test
    void findUserOrganizations_softDeletedMembership_isIgnored() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at, deleted_at) VALUES (UUID(), ?, ?, ?, NOW(6), NOW(6))")
                .setParameter(1, ORG_1).setParameter(2, USER_ID).setParameter(3, "ADMIN_ORG")
                .executeUpdate();
        tx.commit();

        assertThat(sut.findUserOrganizations(USER_ID)).isEmpty();
    }

    @Test
    void findUserOrganizations_softDeletedOrganization_isIgnored() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_1).setParameter(2, USER_ID).setParameter(3, "ADMIN_ORG")
                .executeUpdate();
        em.createNativeQuery("UPDATE organizations SET deleted_at = NOW(6) WHERE id = ?")
                .setParameter(1, ORG_1)
                .executeUpdate();
        tx.commit();

        assertThat(sut.findUserOrganizations(USER_ID)).isEmpty();
    }

    @Test
    void searchActiveMembers_withoutSearch_returnsAllOrderedByName() throws Exception {
        givenMember(ORG_1, USER_C, "ALUNO", false);
        givenMember(ORG_1, USER_A, "GESTOR", false);
        givenMember(ORG_1, USER_B, "PROFESSOR", false);

        var result = sut.searchActiveMembers(ORG_1, null, 0, 20);

        assertThat(result.content()).extracting(OrganizationMember::getUserId)
                .containsExactly(USER_A, USER_B, USER_C);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.number()).isZero();
        assertThat(result.size()).isEqualTo(20);
    }

    @Test
    void searchActiveMembers_byPartialName_ignoresCase() throws Exception {
        givenMember(ORG_1, USER_A, "GESTOR", false);
        givenMember(ORG_1, USER_B, "PROFESSOR", false);

        var result = sut.searchActiveMembers(ORG_1, "BEATRIZ", 0, 20);

        assertThat(result.content()).extracting(OrganizationMember::getUserId).containsExactly(USER_A);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void searchActiveMembers_byPartialEmail_matches() throws Exception {
        givenMember(ORG_1, USER_B, "PROFESSOR", false);
        givenMember(ORG_1, USER_C, "ALUNO", false);

        var result = sut.searchActiveMembers(ORG_1, "example.org", 0, 20);

        assertThat(result.content()).extracting(OrganizationMember::getUserId).containsExactly(USER_C);
    }

    @Test
    void searchActiveMembers_softDeletedMembership_isIgnored() throws Exception {
        givenMember(ORG_1, USER_A, "GESTOR", true);
        givenMember(ORG_1, USER_B, "PROFESSOR", false);

        var result = sut.searchActiveMembers(ORG_1, null, 0, 20);

        assertThat(result.content()).extracting(OrganizationMember::getUserId).containsExactly(USER_B);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void searchActiveMembers_memberOfAnotherOrganization_isIgnored() throws Exception {
        givenMember(ORG_2, USER_A, "GESTOR", false);
        givenMember(ORG_1, USER_B, "PROFESSOR", false);

        assertThat(sut.searchActiveMembers(ORG_1, null, 0, 20).content())
                .extracting(OrganizationMember::getUserId).containsExactly(USER_B);
    }

    @Test
    void searchActiveMembers_paginates_reportingTotals() throws Exception {
        givenMember(ORG_1, USER_A, "GESTOR", false);
        givenMember(ORG_1, USER_B, "PROFESSOR", false);
        givenMember(ORG_1, USER_C, "ALUNO", false);

        var first = sut.searchActiveMembers(ORG_1, null, 0, 2);
        assertThat(first.content()).extracting(OrganizationMember::getUserId)
                .containsExactly(USER_A, USER_B);
        assertThat(first.totalElements()).isEqualTo(3);
        assertThat(first.totalPages()).isEqualTo(2);

        var second = sut.searchActiveMembers(ORG_1, null, 1, 2);
        assertThat(second.content()).extracting(OrganizationMember::getUserId)
                .containsExactly(USER_C);
        assertThat(second.number()).isEqualTo(1);
    }

    private void insertUser(String id, String fullName, String email) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id)
                .setParameter(2, fullName)
                .setParameter(3, email)
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private void givenMember(String orgId, String userId, String role, boolean removed) throws Exception {
        tx.begin();
        em.createNativeQuery(
                        "INSERT INTO organization_members (id, organization_id, user_id, role, joined_at, deleted_at) " +
                        "VALUES (UUID(), ?, ?, ?, NOW(6), " + (removed ? "NOW(6)" : "NULL") + ")")
                .setParameter(1, orgId).setParameter(2, userId).setParameter(3, role)
                .executeUpdate();
        tx.commit();
    }
}
