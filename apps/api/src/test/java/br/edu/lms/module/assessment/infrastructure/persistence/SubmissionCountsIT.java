package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.domain.model.SubmissionCounts;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A contagem de submissões por tarefa sai numa consulta agregada só. Este teste
 * cobre os casos que a agregação erra com facilidade: tarefa sem submissão,
 * submissão apagada por soft delete e tarefa de outra organização.
 */
@QuarkusTest
class SubmissionCountsIT {

    static final String ORG_ID = "54000000-5400-5400-5400-540000000001";
    static final String OTHER_ORG_ID = "54000000-5400-5400-5400-540000000009";
    static final String TEACHER_ID = "54000000-5400-5400-5400-540000000002";
    static final String STUDENT_A = "54000000-5400-5400-5400-540000000003";
    static final String STUDENT_B = "54000000-5400-5400-5400-540000000004";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject SubmissionRepository sut;

    String subjectId;
    String taskSemSubmissao;
    String taskSoPendentes;
    String taskMista;
    String taskComRemovida;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(TEACHER_ID, "Professor Counts IT");
        insertUser(STUDENT_A, "Aluno A Counts IT");
        insertUser(STUDENT_B, "Aluno B Counts IT");
        insertOrg(ORG_ID, "Counts Test Org");
        insertOrg(OTHER_ORG_ID, "Counts Other Org");

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina Counts IT")
                .executeUpdate();

        taskSemSubmissao = insertTask("Sem submissão");
        taskSoPendentes = insertTask("Só pendentes");
        taskMista = insertTask("Mista");
        taskComRemovida = insertTask("Com removida");

        insertSubmission(taskSoPendentes, STUDENT_A, "SUBMITTED", null);
        insertSubmission(taskSoPendentes, STUDENT_B, "SUBMITTED", null);

        insertSubmission(taskMista, STUDENT_A, "SUBMITTED", null);
        insertSubmission(taskMista, STUDENT_B, "EVALUATED", null);

        insertSubmission(taskComRemovida, STUDENT_A, "SUBMITTED", null);
        insertSubmission(taskComRemovida, STUDENT_B, "SUBMITTED", LocalDateTime.now());
        tx.commit();
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private void insertOrg(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, id).setParameter(2, name).setParameter(3, TEACHER_ID)
                .executeUpdate();
    }

    private String insertTask(String title) {
        String id = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, id).setParameter(2, subjectId).setParameter(3, ORG_ID)
                .setParameter(4, TEACHER_ID).setParameter(5, title).setParameter(6, "Enunciado")
                .setParameter(7, LocalDateTime.now().plusDays(7)).setParameter(8, "PUBLISHED")
                .executeUpdate();
        return id;
    }

    /** `deletedAt` não nulo = submissão apagada por soft delete. */
    private void insertSubmission(String taskId, String studentId, String status, LocalDateTime deletedAt) {
        em.createNativeQuery("""
                        INSERT INTO task_submissions (id, task_id, student_id, organization_id, text_response, status, created_at, updated_at, deleted_at)
                        VALUES (?,?,?,?,?,?,NOW(6),NOW(6),?)
                        """)
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskId)
                .setParameter(3, studentId).setParameter(4, ORG_ID).setParameter(5, "Resposta")
                .setParameter(6, status).setParameter(7, deletedAt)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM task_submissions WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id IN (?,?)").setParameter(1, ORG_ID).setParameter(2, OTHER_ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, STUDENT_A).setParameter(3, STUDENT_B).executeUpdate();
        tx.commit();
    }

    @Test
    void countsEveryTaskInASingleCall() {
        Map<String, SubmissionCounts> counts = sut.countByTasks(
                List.of(taskSemSubmissao, taskSoPendentes, taskMista, taskComRemovida), ORG_ID);

        assertThat(counts.get(taskSoPendentes)).isEqualTo(new SubmissionCounts(2, 2));
        assertThat(counts.get(taskMista)).isEqualTo(new SubmissionCounts(2, 1));
    }

    @Test
    void aTaskWithoutSubmissionsIsAbsentFromTheMap() {
        Map<String, SubmissionCounts> counts = sut.countByTasks(List.of(taskSemSubmissao), ORG_ID);

        assertThat(counts).doesNotContainKey(taskSemSubmissao);
    }

    @Test
    void aSoftDeletedSubmissionIsNotCounted() {
        Map<String, SubmissionCounts> counts = sut.countByTasks(List.of(taskComRemovida), ORG_ID);

        assertThat(counts.get(taskComRemovida)).isEqualTo(new SubmissionCounts(1, 1));
    }

    @Test
    void anotherOrganizationSeesNothing() {
        Map<String, SubmissionCounts> counts = sut.countByTasks(List.of(taskMista), OTHER_ORG_ID);

        assertThat(counts).isEmpty();
    }

    @Test
    void anEmptyTaskListDoesNotHitTheDatabase() {
        assertThat(sut.countByTasks(List.of(), ORG_ID)).isEmpty();
        assertThat(sut.countByTasks(null, ORG_ID)).isEmpty();
    }
}
