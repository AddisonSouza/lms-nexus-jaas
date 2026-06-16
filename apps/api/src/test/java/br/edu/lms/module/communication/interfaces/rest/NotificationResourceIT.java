package br.edu.lms.module.communication.interfaces.rest;

import io.quarkus.redis.datasource.RedisDataSource;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class NotificationResourceIT {

    static final String ORG_ID = "10000000-1000-1000-1000-100000000001";
    static final String USER_ID = "10000000-1000-1000-1000-100000000002";
    static final String OTHER_USER_ID = "10000000-1000-1000-1000-100000000003";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject RedisDataSource redis;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(USER_ID, "Notification User IT");
        insertUser(OTHER_USER_ID, "Other Notification User IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID)
                .setParameter(2, "Notification Test Org")
                .setParameter(3, USER_ID)
                .executeUpdate();
        tx.commit();

        redis.value(Long.class).set("communication:unread-count:" + USER_ID, 0L);
        redis.value(Long.class).set("communication:unread-count:" + OTHER_USER_ID, 0L);
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM notifications WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)")
                .setParameter(1, USER_ID).setParameter(2, OTHER_USER_ID)
                .executeUpdate();
        tx.commit();

        redis.key().del("communication:unread-count:" + USER_ID);
        redis.key().del("communication:unread-count:" + OTHER_USER_ID);
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id)
                .setParameter(2, name)
                .setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private String insertNotification(String userId, boolean read) throws Exception {
        var id = java.util.UUID.randomUUID().toString();
        tx.begin();
        em.createNativeQuery("INSERT INTO notifications (id, user_id, organization_id, type, reference_id, title, message, action_link, read_at, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,NOW(6))")
                .setParameter(1, id)
                .setParameter(2, userId)
                .setParameter(3, ORG_ID)
                .setParameter(4, "ANNOUNCEMENT_POSTED")
                .setParameter(5, "ref-1")
                .setParameter(6, "Novo aviso")
                .setParameter(7, "Um novo aviso foi publicado na turma.")
                .setParameter(8, "/classrooms/classroom-1")
                .setParameter(9, read ? java.time.LocalDateTime.now() : null)
                .executeUpdate();
        tx.commit();
        if (!read) {
            redis.value(Long.class).incrby("communication:unread-count:" + userId, 1);
        }
        return id;
    }

    // --- GET /notifications ---

    @Test
    void list_withoutAuth_returns401() {
        given()
                .when().get("/notifications")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void list_withoutNotifications_returns200EmptyAndZeroUnread() {
        given()
                .when().get("/notifications")
                .then()
                .statusCode(200)
                .body("items", hasSize(0))
                .body("unreadCount", equalTo(0));
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void list_returnsOnlyOwnNotificationsOrderedByCreatedAtDesc() throws Exception {
        insertNotification(OTHER_USER_ID, false);
        var first = insertNotification(USER_ID, false);
        Thread.sleep(10);
        var second = insertNotification(USER_ID, false);

        given()
                .when().get("/notifications")
                .then()
                .statusCode(200)
                .body("items", hasSize(2))
                .body("items[0].id", equalTo(second))
                .body("items[1].id", equalTo(first))
                .body("unreadCount", equalTo(2));
    }

    // --- PATCH /notifications/{id}/read ---

    @Test
    @TestSecurity(user = USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void markRead_ownUnreadNotification_returns200AndDecrementsCounter() throws Exception {
        var id = insertNotification(USER_ID, false);

        given()
                .when().patch("/notifications/{id}/read", id)
                .then()
                .statusCode(200)
                .body("read", equalTo(true));

        given()
                .when().get("/notifications")
                .then().statusCode(200).body("unreadCount", equalTo(0));
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void markRead_alreadyRead_returns200WithoutChangingCounter() throws Exception {
        var id = insertNotification(USER_ID, true);

        given()
                .when().patch("/notifications/{id}/read", id)
                .then()
                .statusCode(200)
                .body("read", equalTo(true));

        given()
                .when().get("/notifications")
                .then().statusCode(200).body("unreadCount", equalTo(0));
    }

    @Test
    @TestSecurity(user = OTHER_USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = OTHER_USER_ID), @Claim(key = "org", value = ORG_ID)})
    void markRead_notOwner_returns403() throws Exception {
        var id = insertNotification(USER_ID, false);

        given()
                .when().patch("/notifications/{id}/read", id)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void markRead_nonExistent_returns404() {
        given()
                .when().patch("/notifications/{id}/read", "nonexistent-id")
                .then().statusCode(404);
    }

    // --- PATCH /notifications/read-all ---

    @Test
    @TestSecurity(user = USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void markAllRead_withUnreadNotifications_returns200WithZeroUnreadCount() throws Exception {
        insertNotification(USER_ID, false);
        insertNotification(USER_ID, false);

        given()
                .when().patch("/notifications/read-all")
                .then()
                .statusCode(200)
                .body("unreadCount", equalTo(0));

        given()
                .when().get("/notifications")
                .then().statusCode(200).body("unreadCount", equalTo(0));
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID)})
    void markAllRead_withoutUnreadNotifications_returns200WithZeroUnreadCount() {
        given()
                .when().patch("/notifications/read-all")
                .then()
                .statusCode(200)
                .body("unreadCount", equalTo(0));
    }
}
