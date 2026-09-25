package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.port.in.SubjectDirectoryPort;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class SubjectDirectoryServiceIT {

    static final String ORG_ID = "43100000-4310-4310-4310-431000000001";
    static final String OTHER_ORG_ID = "43100000-4310-4310-4310-431000000002";
    static final String TEACHER_ID = "43100000-4310-4310-4310-431000000003";
    static final String FORMER_TEACHER_ID = "43100000-4310-4310-4310-431000000004";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject SubjectDirectoryPort sut;

    String classroomAId;
    String classroomBId;
    String subjectId;
    String deletedSubjectId;
    String teacherMemberId;
    String formerTeacherMemberId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(TEACHER_ID, "Professor Directory IT");
        insertUser(FORMER_TEACHER_ID, "Ex-professor Directory IT");
        insertOrganization(ORG_ID, "Subject Directory Org");
        insertOrganization(OTHER_ORG_ID, "Subject Directory Other Org");

        classroomAId = insertClassroom("DIRA01");
        classroomBId = insertClassroom("DIRB01");
        subjectId = insertSubject("Matemática", false);
        deletedSubjectId = insertSubject("Física", true);

        linkClassroom(subjectId, classroomAId);
        linkClassroom(deletedSubjectId, classroomBId);

        teacherMemberId = insertMember(TEACHER_ID, false);
        formerTeacherMemberId = insertMember(FORMER_TEACHER_ID, true);
        linkTeacher(subjectId, teacherMemberId);
        linkTeacher(subjectId, formerTeacherMemberId);
        linkTeacher(deletedSubjectId, teacherMemberId);
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM subject_teachers WHERE subject_id IN (?,?)")
                .setParameter(1, subjectId).setParameter(2, deletedSubjectId).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_classrooms WHERE subject_id IN (?,?)")
                .setParameter(1, subjectId).setParameter(2, deletedSubjectId).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)")
                .setParameter(1, ORG_ID).setParameter(2, OTHER_ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, FORMER_TEACHER_ID).executeUpdate();
        tx.commit();
    }

    @Test
    void findClassroomIdsBySubject_returnsLinkedClassrooms() {
        assertThat(sut.findClassroomIdsBySubject(subjectId)).containsExactly(classroomAId);
    }

    @Test
    void findSubjectIdsByClassrooms_returnsSubjectsOfEveryClassroom_andEmptyForNoClassrooms() {
        assertThat(sut.findSubjectIdsByClassrooms(List.of(classroomAId, classroomBId)))
                .containsExactlyInAnyOrder(subjectId, deletedSubjectId);
        assertThat(sut.findSubjectIdsByClassrooms(List.of())).isEmpty();
    }

    @Test
    void findTeacherUserIdsBySubject_skipsRemovedMembers() {
        assertThat(sut.findTeacherUserIdsBySubject(subjectId)).containsExactly(TEACHER_ID);
    }

    @Test
    void isTeacherOfSubject_withoutOrganization_matchesActiveMembersOnly() {
        assertThat(sut.isTeacherOfSubject(subjectId, TEACHER_ID)).isTrue();
        assertThat(sut.isTeacherOfSubject(subjectId, FORMER_TEACHER_ID)).isFalse();
    }

    @Test
    void isTeacherOfSubject_withOrganization_requiresActiveSubjectInSameOrganization() {
        assertThat(sut.isTeacherOfSubject(subjectId, ORG_ID, TEACHER_ID)).isTrue();
        assertThat(sut.isTeacherOfSubject(subjectId, OTHER_ORG_ID, TEACHER_ID)).isFalse();
        assertThat(sut.isTeacherOfSubject(deletedSubjectId, ORG_ID, TEACHER_ID)).isFalse();
        assertThat(sut.isTeacherOfSubject(subjectId, ORG_ID, FORMER_TEACHER_ID)).isFalse();
    }

    @Test
    void existsSubject_ignoresDeletedAndOtherOrganizations() {
        assertThat(sut.existsSubject(subjectId, ORG_ID)).isTrue();
        assertThat(sut.existsSubject(subjectId, OTHER_ORG_ID)).isFalse();
        assertThat(sut.existsSubject(deletedSubjectId, ORG_ID)).isFalse();
    }

    @Test
    void findSubjectNamesByIds_includesDeletedSubjects() {
        assertThat(sut.findSubjectNamesByIds(List.of(subjectId, deletedSubjectId)))
                .containsEntry(subjectId, "Matemática")
                .containsEntry(deletedSubjectId, "Física")
                .hasSize(2);
        assertThat(sut.findSubjectNamesByIds(List.of())).isEmpty();
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private void insertOrganization(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, id).setParameter(2, name).setParameter(3, TEACHER_ID)
                .executeUpdate();
    }

    private String insertClassroom(String inviteCode) {
        String id = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO classrooms (id, organization_id, name, academic_period, status, invite_code, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, id).setParameter(2, ORG_ID).setParameter(3, "Turma " + inviteCode)
                .setParameter(4, "2026.2").setParameter(5, "ACTIVE").setParameter(6, inviteCode)
                .executeUpdate();
        return id;
    }

    private String insertSubject(String name, boolean deleted) {
        String id = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at, deleted_at) " +
                        "VALUES (?,?,?,NOW(6),NOW(6)," + (deleted ? "NOW(6)" : "NULL") + ")")
                .setParameter(1, id).setParameter(2, ORG_ID).setParameter(3, name)
                .executeUpdate();
        return id;
    }

    private String insertMember(String userId, boolean deleted) {
        String id = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at, deleted_at) " +
                        "VALUES (?,?,?,?,NOW(6)," + (deleted ? "NOW(6)" : "NULL") + ")")
                .setParameter(1, id).setParameter(2, ORG_ID).setParameter(3, userId).setParameter(4, "PROFESSOR")
                .executeUpdate();
        return id;
    }

    private void linkClassroom(String subject, String classroom) {
        em.createNativeQuery("INSERT INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subject).setParameter(2, classroom)
                .executeUpdate();
    }

    private void linkTeacher(String subject, String member) {
        em.createNativeQuery("INSERT INTO subject_teachers (subject_id, member_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subject).setParameter(2, member)
                .executeUpdate();
    }
}
