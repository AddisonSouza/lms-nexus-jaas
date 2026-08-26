package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.organization.domain.model.MemberRole;
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
        em.createNativeQuery("DELETE FROM organization_members WHERE user_id = ?").setParameter(1, USER_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)").setParameter(1, ORG_1).setParameter(2, ORG_2).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, USER_ID).executeUpdate();
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
    void findOrganizationsByUser_multipleActiveMemberships_returnsAll() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_1).setParameter(2, USER_ID).setParameter(3, "ADMIN_ORG")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (UUID(), ?, ?, ?, NOW(6))")
                .setParameter(1, ORG_2).setParameter(2, USER_ID).setParameter(3, "PROFESSOR")
                .executeUpdate();
        tx.commit();

        var result = sut.findOrganizationsByUser(USER_ID);

        assertThat(result).hasSize(2);
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
}
